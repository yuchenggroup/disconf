package com.baidu.disconf.client.core.processor.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.common.model.DisConfCommonModel;
import com.baidu.disconf.client.common.model.DisconfCenterFile;
import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.core.filetype.FileTypeProcessorUtils;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessor;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.store.DisconfStoreProcessor;
import com.baidu.disconf.client.store.DisconfStoreProcessorFactory;
import com.baidu.disconf.client.store.processor.model.DisconfValue;
import com.baidu.disconf.client.watch.WatchMgr;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;
import com.github.knightliao.apollo.utils.data.GsonUtils;

/**
 * 配置文件处理器实现
 *
 * @author liaoqiqi
 * @version 2014-8-4
 */
public class DisconfFileCoreProcessorImpl implements DisconfCoreProcessor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfFileCoreProcessorImpl.class);

    // 监控器
    private WatchMgr watchMgr = null;

    // 抓取器
    private FetcherMgr fetcherMgr = null;

    // 仓库算子
    private DisconfStoreProcessor disconfStoreProcessor = DisconfStoreProcessorFactory.getDisconfStoreFileProcessor();

    public DisconfFileCoreProcessorImpl(WatchMgr watchMgr, FetcherMgr fetcherMgr) {

        this.fetcherMgr = fetcherMgr;
        this.watchMgr = watchMgr;
    }

    /**
     *
     */
    @Override
    public void processAllItems() {

        /**
         * 配置文件列表处理
         */
        for (String fileName : disconfStoreProcessor.getConfKeySet()) {

            processOneItem(fileName);
        }
    }

    @Override
    public void processOneItem(String key) {

        LOGGER.debug("==============\tstart to process disconf file: " + key +
                         "\t=============================");

        DisconfCenterFile disconfCenterFile = (DisconfCenterFile) disconfStoreProcessor.getConfData(key);

        try {
            updateOneConfFile(key, disconfCenterFile);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    /**
     * 更新 一個配置文件, 下载、注入到仓库、Watch 三步骤
     */
    private void updateOneConfFile(String fileName, DisconfCenterFile disconfCenterFile) throws Exception {

        if (disconfCenterFile == null) {
            throw new Exception("cannot find disconfCenterFile " + fileName);
        }

        String filePath = fileName;
        Map<String, Object> dataMap = new HashMap<String, Object>();

        //
        // 开启disconf才需要远程下载, 否则就本地就好
        //
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {

            //
            // 下载配置
            //
            try {

                String url = disconfCenterFile.getRemoteServerUrl();
                filePath = fetcherMgr.downloadFileFromServer(url, fileName);

            } catch (Exception e) {

                //
                // 下载失败了, 尝试使用本地的配置
                //

                LOGGER.error(e.toString(), e);
                LOGGER.warn("using local properties in class path: " + fileName);

                // change file path
                filePath = fileName;
            }
            LOGGER.debug("download ok.");
        }

        try {
            dataMap = FileTypeProcessorUtils.getKvMap(disconfCenterFile.getSupportFileTypeEnum(), fileName);
        } catch (Exception e) {
            LOGGER.error("cannot get kv data for " + filePath, e);
        }

        //
        // 注入到仓库中
        //
        disconfStoreProcessor.inject2Store(fileName, new DisconfValue(null, dataMap));
        LOGGER.debug("inject ok.");

        //
        // 开启disconf才需要进行watch
        //
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {
            //
            // Watch
            //
            DisConfCommonModel disConfCommonModel = disconfStoreProcessor.getCommonModel(fileName);
            if (watchMgr != null) {
                watchMgr.watchPath(this, disConfCommonModel, fileName, DisConfigTypeEnum.FILE,
                                      GsonUtils.toJson(disconfCenterFile.getKV()));
                LOGGER.debug("watch ok.");
            } else {
                LOGGER.warn("cannot monitor {} because watch mgr is null", fileName);
            }
        }
    }

    /**
     * 更新消息: 某个配置文件 + 回调
     */
    @Override
    public void updateOneConfAndCallback(String key) throws Exception {

        // 更新 配置
        updateOneConf(key);

        // 回调
        DisconfCoreProcessUtils.callOneConf(disconfStoreProcessor, key);
    }

    /**
     * 更新消息：某个配置文件
     */
    private void updateOneConf(String fileName) throws Exception {

        DisconfCenterFile disconfCenterFile = (DisconfCenterFile) disconfStoreProcessor.getConfData(fileName);

        if (disconfCenterFile != null) {

            // 更新仓库
            updateOneConfFile(fileName, disconfCenterFile);

            // 更新实例
            inject2OneConf(fileName, disconfCenterFile);
        }
    }

    /**
     * 为某个配置文件进行注入实例中
     */
    private void inject2OneConf(String fileName, DisconfCenterFile disconfCenterFile) {

        if (disconfCenterFile == null) {
            return;
        }

        try {

            //
            // 获取实例
            //

            Object object;
            try {

                object = disconfCenterFile.getObject();
                if (object == null) {
                    object = DisconfCoreProcessUtils.getSpringBean(disconfCenterFile.getCls());
                }

            } catch (Exception e) {

                LOGGER.debug(disconfCenterFile.getCls() + " may be a non-java-bean");
                object = null;
            }

            // 注入实体中
            disconfStoreProcessor.inject2Instance(object, fileName);

        } catch (Exception e) {
            LOGGER.warn(e.toString(), e);
        }
    }

    @Override
    public void inject2Conf() {

        /**
         * 配置文件列表处理
         */
        for (String key : disconfStoreProcessor.getConfKeySet()) {

            LOGGER.debug("==============\tstart to inject value to disconf file item instance: " + key +
                             "\t=============================");

            DisconfCenterFile disconfCenterFile = (DisconfCenterFile) disconfStoreProcessor.getConfData(key);

            inject2OneConf(key, disconfCenterFile);
        }
    }
}

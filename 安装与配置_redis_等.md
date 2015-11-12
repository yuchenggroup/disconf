
# 安装与配置_redis_tomcat_mysql_nginx 等



下载页面: [http://redis.io/download](http://redis.io/download)

如果不出问题,

准备目录

	cd /yc
	
	sudo mkdir download
	
	sudo chmod 777 download
	
	cd download

安装 gcc
	
	sudo yum install gcc cc -y

下载安装

	wget http://download.redis.io/releases/redis-3.0.5.tar.gz
	tar xzf redis-3.0.5.tar.gz
	cd redis-3.0.5
	make MALLOC=libc

如果没有 jmalloc, 那么需要指定`make MALLOC=libc`


后台启动:

	src/redis-server &

测试

	src/redis-cli
	
	set name xiaoming
	
	get name
	
	info

关闭服务器
	
	shutdown
	
	quit

至此，redis安装完毕.


## 安装 maven

	sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
	
	sudo yum -y install apache-maven


### 准备目录

	sudo chmod 777 /yc/home
	
	mkdir /yc/home/work/dsp/disconf-rd/online-resources -p
	
	cd /yc/home/devps
	
	mkdir /yc/home/devps/disconf -p
	
	cd /yc/home/devps/disconf


上传文件

	rz
	
	unzip disconf.zip
	
	
	cp disconf-web/src/main/resources/*.properties /yc/home/work/dsp/disconf-rd/online-resources
	
	
	mv /yc/home/work/dsp/disconf-rd/online-resources/application-demo.properties /yc/home/work/dsp/disconf-rd/online-resources/application.properties
	
	
	mkdir /yc/home/devps/disconf/war -p

执行下面的脚本



	ONLINE_CONFIG_PATH=/yc/home/work/dsp/disconf-rd/online-resources
	WAR_ROOT_PATH=/yc/home/devps/disconf/war
	export ONLINE_CONFIG_PATH
	export WAR_ROOT_PATH
	cd /yc/home/devps/disconf/disconf-web
	sh deploy/deploy.sh


如果报错,则将 `deploy/deploy.sh` 和  `build_java.sh` 按照原来的顺序执行即可。[**这里可能是脚本有BUG**]


## 安装MySQL

可以参考这里: [http://blog.csdn.net/renfufei/article/details/17616549](http://blog.csdn.net/renfufei/article/details/17616549)

mysql 密码


	grant all privileges on *.* to 'root'@'%' identified by '123456';


## 安装tomcat

可以参考这里:[http://blog.csdn.net/renfufei/article/details/9733367](http://blog.csdn.net/renfufei/article/details/9733367)


修改配置文件:

	cd /yc/home/devps/tomcat7_80/conf
	
	<Context path="" docBase="/yc/home/devps/disconf/war"></Context>
	

拷贝文件:

	cd /yc/home/devps/disconf/war
	
	cp -rf  html/* ./

启动tomcat

	cd ~
	
	/yc/home/devps/tomcat7_80/bin/startup.sh


其实这里没必要麻烦,直接将 war 包拷贝到 tomcat 的 `webapps` 目录下,成为 ROOT.war 更方便，记得把原来的 ROOT 重命名。


-- 安装 nginx


	sudo rpm -ivh http://nginx.org/packages/centos/6/noarch/RPMS/nginx-release-centos-6-0.el6.ngx.noarch.rpm
	
	sudo yum install -y nginx
	yum info nginx
	
	service nginx start

可以参考这里: [https://github.com/cncounter/translation/blob/master/tiemao_2015/15_Nginx/nginx.md](https://github.com/cncounter/translation/blob/master/tiemao_2015/15_Nginx/nginx.md)




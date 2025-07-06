FROM centos:7

# 设置环境变量和全局环境变量
ENV HADOOP_HOME=/usr/local/hadoop
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
ENV HIVE_HOME=/usr/local/hive
ENV PATH=$HIVE_HOME/bin:$PATH

COPY hadoop-3.3.6 /usr/local/hadoop
COPY apache-hive-3.1.2-bin /usr/local/hive
COPY hive-site.xml $HIVE_HOME/conf/hive-site.xml
COPY mysql-connector-java-5.1.49/mysql-connector-java-5.1.49.jar /usr/local/hive/lib
COPY ssh_keys/ /home/hadoop/.ssh/

# root用户层
RUN curl https://mirrors.aliyun.com/repo/Centos-7.repo -o /etc/yum.repos.d/CentOS-Base.repo && \
    # 安装JAVA和SSH
    yum makecache && \
    yum install -y \
          java-1.8.0-openjdk-devel \
          openssh-clients \
          openssh-server && \
    yum clean all && \
    rm -rf /var/cache/yum/* && \
    # 设置JAVA,HADOOP和Hive的环境变量
    mkdir -p $HADOOP_HOME/etc/hadoop && \
    echo "export JAVA_HOME=$JAVA_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> /etc/profile && \
    echo "export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin" >> /etc/profile && \
    echo "export HIVE_HOME=$HIVE_HOME" >> /etc/profile && \
    echo "export PATH=\$PATH:\$HIVE_HOME/bin" >> /etc/profile && \
    echo "export HADDOP_HOME=/usr/local/hadoop" >> $HIVE_HOME/conf/hive-env.sh && \
    echo "export HIVE_CONF_DIR=/usr/local/hive/conf" >> $HIVE_HOME/conf/hive-env.sh && \
    echo "export HIVE_AUX_JARS_PATH=/usr/local/hive/lib" >> $HIVE_HOME/conf/hive-env.sh && \
    # 强制Hive使用Hadoop的Guava
    rm -f $HIVE_HOME/lib/guava-*.jar && \
    cp $HADOOP_HOME/share/hadoop/common/lib/guava-27.0-jre.jar $HIVE_HOME/lib/ && \
    # 创建普通用户，分配HADOOP目录权限
    adduser hadoop && \
    echo "hadoop:hadoop" | chpasswd && \
    echo "root:root" | chpasswd && \
    chown -R hadoop /usr/local/hadoop && \
    # 配置SSH服务
    sed -i 's/#PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    sed -i 's/#UsePAM.*/UsePAM no/' /etc/ssh/sshd_config && \
    echo "AllowUsers root hadoop" >> /etc/ssh/sshd_config && \
    # 生成SSH主机密钥
    ssh-keygen -A && \
    mkdir -p /var/run/sshd && \
    chmod 755 /var/run/sshd && \
    chown -R hadoop /home/hadoop/

# 配置core-site.xml
RUN cat > $HADOOP_HOME/etc/hadoop/core-site.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 配置 HDFS 主机地址与端口号 -->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://nn:9000</value>
    </property>
    <!-- 配置 Hadoop 的临时文件目录 -->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>file:///home/hadoop/tmp</value>
    </property>
    <property>
      <name>hadoop.proxyuser.hadoop.groups</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hadoop.hosts</name>
        <value>*</value>
    </property>
</configuration>
EOF

# 配置hdfs-site.xml
RUN cat > $HADOOP_HOME/etc/hadoop/hdfs-site.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 每个数据块复制 5 份存储 -->
    <property>
        <name>dfs.replication</name>
        <value>5</value>
    </property>
    <!-- 设置储存命名信息的目录 -->
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///home/hadoop/hdfs/name</value>
    </property>
    <!-- 设置数据节点数据目录 -->
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>file:///home/hadoop/hdfs/data</value>
    </property>
    <!-- 设置Web UI端口 -->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>nn:9870</value>
    </property>
    <!-- 开启WebHDFS -->
    <property>
        <name>dfs.webhdfs.enabled</name>
        <value>true</value>
    </property>
</configuration>
EOF

# 配置mapred-site.xml
RUN cat > $HADOOP_HOME/etc/hadoop/mapred-site.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 指定MapReduce运行框架为YARN -->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    
    <!-- 设置MapReduce应用程序的类路径 -->
    <property>
        <name>mapreduce.application.classpath</name>
        <value>
            $HADOOP_HOME/share/hadoop/mapreduce/*,
            $HADOOP_HOME/share/hadoop/mapreduce/lib/*,
            $HADOOP_HOME/share/hadoop/common/*,
            $HADOOP_HOME/share/hadoop/common/lib/*,
            $HADOOP_HOME/share/hadoop/yarn/*,
            $HADOOP_HOME/share/hadoop/yarn/lib/*
        </value>
    </property>
    
    <!-- 设置Application Master的环境变量 -->
    <property>
        <name>yarn.app.mapreduce.am.env</name>
        <value>HADOOP_MAPRED_HOME=$HADOOP_HOME</value>
    </property>
    
    <!-- 设置Map任务的环境变量 -->
    <property>
        <name>mapreduce.map.env</name>
        <value>HADOOP_MAPRED_HOME=$HADOOP_HOME</value>
    </property>
    
    <!-- 设置Reduce任务的环境变量 -->
    <property>
        <name>mapreduce.reduce.env</name>
        <value>HADOOP_MAPRED_HOME=$HADOOP_HOME</value>
    </property>
    
    <!-- 设置Application Master的内存限制为512MB -->
    <property>
        <name>yarn.app.mapreduce.am.resource.mb</name>
        <value>512</value>
    </property>
    
    <!-- 设置每个Map任务的内存限制为512MB -->
    <property>
        <name>mapreduce.map.memory.mb</name>
        <value>512</value>
    </property>
    
    <!-- 设置每个Reduce任务的内存限制为512MB -->
    <property>
        <name>mapreduce.reduce.memory.mb</name>
        <value>512</value>
    </property>
</configuration>
EOF

# 配置yarn-site.xml
RUN cat > $HADOOP_HOME/etc/hadoop/yarn-site.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 指定NodeManager的辅助服务为mapreduce_shuffle -->
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    
    <!-- 指定shuffle处理的类 -->
    <property>
        <name>yarn.nodemanager.aux-services.mapreduce_shuffle.class</name>
        <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
    
    <!-- 指定ResourceManager的主机名 -->
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>nn</value>
    </property>
    
    <!-- ResourceManager与客户端通信的地址 -->
    <property>
        <name>yarn.resourcemanager.address</name>
        <value>nn:8032</value>
    </property>
    
    <!-- ResourceManager与调度器通信的地址 -->
    <property>
        <name>yarn.resourcemanager.scheduler.address</name>
        <value>nn:8030</value>
    </property>
    
    <!-- ResourceManager与NodeManager通信的地址 -->
    <property>
        <name>yarn.resourcemanager.resource-tracker.address</name>
        <value>nn:8031</value>
    </property>
    
    <!-- ResourceManager管理接口地址 -->
    <property>
        <name>yarn.resourcemanager.admin.address</name>
        <value>nn:8033</value>
    </property>
    
    <!-- ResourceManager Web UI地址 -->
    <property>
        <name>yarn.resourcemanager.webapp.address</name>
        <value>nn:8088</value>
    </property>
    
    <!-- 每个NodeManager可用的物理内存量，设置为512MB -->
    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>512</value>
    </property>
    
    <!-- 单个容器可申请的最小内存量，设置为256MB -->
    <property>
        <name>yarn.scheduler.minimum-allocation-mb</name>
        <value>256</value>
    </property>
    
    <!-- 单个容器可申请的最大内存量，设置为512MB -->
    <property>
        <name>yarn.scheduler.maximum-allocation-mb</name>
        <value>512</value>
    </property>
    
    <!-- 关闭虚拟内存检查，避免因虚拟内存限制导致任务失败 -->
    <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
    </property>
    
    <!-- 设置每个容器可用的虚拟内存与物理内存的比例 -->
    <property>
        <name>yarn.nodemanager.vmem-pmem-ratio</name>
        <value>2.1</value>
    </property>
</configuration>
EOF

USER hadoop
WORKDIR /home/hadoop

# hadoop用户层
# 修复命令行提示符
RUN cp /etc/skel/.bash* ~/ && \
    echo "export PS1='[\u@\h \W]\$ '" >> ~/.bashrc && \
    echo "source /etc/profile" >> ~/.bashrc && \
    chmod 700 ~/.ssh  && \
    chmod 600 ~/.ssh/authorized_keys && \
    # 将工作节点添加到Hadoop配置文件中
    echo "dn1" >> $HADOOP_HOME/etc/hadoop/workers && \
    echo "dn2" >> $HADOOP_HOME/etc/hadoop/workers && \
    echo "dn3" >> $HADOOP_HOME/etc/hadoop/workers && \
    echo "dn4" >> $HADOOP_HOME/etc/hadoop/workers


USER root
EXPOSE 22 9870 8088 19888 9864 9866 9867 9868 9000 10000 8042 8030 8031 8032 8033
CMD ["/usr/sbin/sshd", "-D"]



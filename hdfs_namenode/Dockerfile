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
EXPOSE 22 9870 8088 19888 9864 9866 9867 9868 9000 10000 9083
CMD ["/usr/sbin/sshd", "-D"]



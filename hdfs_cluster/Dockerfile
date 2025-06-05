FROM centos:7

# 设置环境变量和全局环境变量
ENV HADOOP_HOME=/usr/local/hadoop
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

COPY hadoop-3.3.6 /usr/local/hadoop

# root用户层
RUN curl https://repo.huaweicloud.com/repository/conf/CentOS-7-reg.repo -o /etc/yum.repos.d/CentOS-Base.repo && \
    sed -i 's/mirrors.aliyun.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/yum.repos.d/CentOS-Base.repo && \
    # 安装JAVA和SSH
    yum makecache && \
    yum install -y \
          java-1.8.0-openjdk-devel \
          openssh-clients \
          openssh-server && \
    yum clean all && \
    rm -rf /var/cache/yum/* && \
    # 设置JAVA和HADOOP环境变量
    mkdir -p $HADOOP_HOME/etc/hadoop && \
    echo "export JAVA_HOME=$JAVA_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> /etc/profile && \
    echo "export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin" >> /etc/profile && \
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
    chmod 755 /var/run/sshd

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
</configuration>
EOF

# 配置hdfs-site.xml
RUN cat > $HADOOP_HOME/etc/hadoop/hdfs-site.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 每个数据块复制 3 份存储 -->
    <property>
        <name>dfs.replication</name>
        <value>3</value>
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
# 生成用户RSA密钥对，将公钥添加到认证文件
# 用该镜像同时运行多个数据节点，
# 节点的公钥都已经添加到认证文件中，
# 各节点互信
RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    chmod 700 ~/.ssh  && \
    chmod 600 ~/.ssh/authorized_keys && \
    # 将工作节点添加到Hadoop配置文件中
    echo "dn1" >> $HADOOP_HOME/etc/hadoop/workers && \
    echo "dn2" >> $HADOOP_HOME/etc/hadoop/workers


USER root
EXPOSE 22 9870 8088 19888 9864 9866 9867 9868 9000
CMD ["/usr/sbin/sshd", "-D"]



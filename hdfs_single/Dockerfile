FROM centos:7

# 设置环境变量和全局环境变量
ENV HADOOP_HOME=/usr/local/hadoop
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

COPY hadoop-3.3.6 /usr/local/hadoop

# 将yum下载环节减少为一层RUN
RUN curl https://repo.huaweicloud.com/repository/conf/CentOS-7-reg.repo -o /etc/yum.repos.d/CentOS-Base.repo && \
    sed -i 's/mirrors.aliyun.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/yum.repos.d/CentOS-Base.repo && \
    yum clean all && \
    yum makecache && \
    yum install -y \
          java-1.8.0-openjdk-devel \
          openssh-clients \
          openssh-server \
          passwd \
          sudo && \
    mkdir -p $HADOOP_HOME/etc/hadoop && \
    echo "export JAVA_HOME=$JAVA_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> /etc/profile && \
    echo "export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin" >> /etc/profile && \
    adduser hadoop

RUN echo "hadoop" | passwd --stdin hadoop && \
    echo "hadoop ALL=(ALL)  ALL" >> /etc/sudoers && \
    chown -R hadoop /usr/local/hadoop && \
    ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    chmod 0600 ~/.ssh/authorized_keys && \
    yum clean all && \
    rm -rf /var/cache/yum/*

USER hadoop
WORKDIR /home/hadoop

EXPOSE 22 9870 8088 19888 9864 9866 9867 9868

CMD ["sudo", "usr/sbin/sshd", "-D"]


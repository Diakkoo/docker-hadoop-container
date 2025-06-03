FROM centos:7

RUN (curl --retry 3 --retry-delay 5 -fL https://repo.huaweicloud.com/repository/conf/CentOS-7-reg.repo -o /etc/yum.repos.d/CentOS-Base.repo || \
     curl --retry 3 --retry-delay 5 -fL http://mirrors.tuna.tsinghua.edu.cn/repo/Centos-7.repo -o /etc/yum.repos.d/CentOS-Base.repo || \
     curl --retry 3 --retry-delay 5 -fL http://mirrors.163.com/.help/CentOS7-Base-163.repo -o /etc/yum.repos.d/CentOS-Base.repo) && \
    sed -i 's/mirrors.aliyun.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/yum.repos.d/CentOS-Base.repo

RUN yum clean all && yum makecache

RUN yum install -y \
         java-1.8.0-openjdk-devel \
         openssh-clients \
         openssh-server \
         passwd \
         sudo

RUN yum clean all && \
    rm -rf /var/cache/yum/*

# 设置环境变量
ENV HADOOP_HOME=/usr/local/hadoop
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

RUN mkdir -p $HADOOP_HOME/etc/hadoop

RUN echo "export JAVA_HOME=$JAVA_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "export HADOOP_HOME=$HADOOP_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh

# 设置全局环境变量
RUN echo "export HADOOP_HOME=$HADOOP_HOME" >> /etc/profile && \
    echo "export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin" >> /etc/profile

COPY hadoop-3.3.6 /usr/local/hadoop

RUN adduser hadoop && \
    echo "hadoop" | passwd --stdin hadoop

RUN echo "hadoop ALL=(ALL)  ALL" >> /etc/sudoers

RUN chown -R hadoop /usr/local/hadoop

USER hadoop
WORKDIR /home/hadoop

RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    chmod 0600 ~/.ssh/authorized_keys

EXPOSE 22 9870 8088 19888 9864 9866 9867 9868

CMD ["sudo", "usr/sbin/sshd", "-D"]

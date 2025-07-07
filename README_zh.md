# Docker Hadoop Container

[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)

简体中文 | [English](./README.md)

**Docker-Compose ---> Flask + Hadoop + Hive + MySQL + YARN**

hadoop-base 镜像的大小为 **3.22GB** （v0.1.0），相比 runoob 教程中的 cluster_protocal 镜像减少了 **48.6%** 🚀

|    镜像    |优化前|优化后|减少量|
|:----------:|:----|:----|:------|
|hadoop-base|6.2GB|3.22GB|2.98GB|

## 资源需求

[Hadoop 3.3.6](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

[Hive 3.1.2](https://dlcdn.apache.org/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz)

[MySQL-Connector-Java-5.1.49](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.49.tar.gz)

## 使用方法

### Flask 容器

1. 拉取 `flask` 目录到本地，然后在 `flask` 目录下执行以下命令构建镜像
    ```
    docker build -t flask .
    ```

2. 执行 `docker images` 查看镜像
    ```
    REPOSITORY      TAG       IMAGE ID       CREATED        SIZE
    flask           latest    5303cfbf82fb   35 hours ago   233MB
    ```

#### Flask 目录结构 🗂️

```
flask/
    ├── app.py
    ├── conf.py
    ├── connection/
    |   ├── __init__.py
    |   └── conn_routes.py
    ├── download/
    |   ├── __init__.py
    |   └── dwl_routes.py
    ├── generate/
    |   ├── __init__.py
    |   └── gnr_routes.py
    ├── upload/
    |   ├── __init__.py
    |   └── upl_routes.py
    ├── templates/
    │   └── index.html
    ├── requirements.txt
    └── Dockerfile
```

---

### Hadoop-Base 容器作为 Datanode

HDFS 容器参考了 [菜鸟教程 Hadoop 教程](https://www.runoob.com/w3cnote/hadoop-tutorial.html)原始镜像大小为 **6.2GB**因此，我编写了一个 `Dockerfile`，集成了所有标准依赖环境和步骤，以实现最小化和便利性

#### Hdfs_Datanode 目录结构 🗂️

构建镜像时，目录结构应包括以下资源：


```
hdfs_datanode/
            ├── ssh_keys/
            |   ├── id_rsa
            |   ├── id_rsa.pub
            |   └── authorized_keys
            ├── hadoop-3.3.6/
            │   ├── bin/
            |   ├── etc/
            |   └── ......
            └── Dockerfile
```
    
1. 拉取 'hdfs_datanode' 目录到本地

2. 解压并将 Hadoop **3.3.6** 二进制文件放置到 `hdfs_datanode` 目录中

    官方网站下载链接：
    [Hadoop 3.3.6 下载地址](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

3. 在 `Dockerfile` 中，我选择将 Hadoop 二进制文件复制到镜像目录 `/usr/local/hadoop`，而不是 .tar.gz 压缩包，以简化步骤并减少存储使用
    ```Dockerfile
    COPY hadoop-3.3.6 /usr/local/hadoop
    ```

4. 执行以下命令构建镜像
    ```
    docker build -t hadoop-base .
    ```

5. 执行命令 `docker images` 查看镜像
    ```
    REPOSITORY      TAG       IMAGE ID       CREATED             SIZE
    hadoop-base    latest    3fd30855b0ac   About an hour ago   3.22GB
    ```

### Hadoop-Hive 容器作为 Namenode

#### Hdfs_Namenode 目录结构 🗂️

构建镜像时，目录结构应包括以下资源：


```
hdfs_namenode/
            ├── ssh_keys/
            |   ├── id_rsa
            |   ├── id_rsa.pub
            |   └── authorized_keys
            ├── hadoop-3.3.6/
            │   ├── bin/
            |   ├── etc/
            |   └── ......
            ├── apache-hive-3.1.2-bin/
            │   ├── bin/
            |   ├── conf/
            |   └── ......
            ├── mysql-connector-java-5.1.49
            │   ├── mysql-connector-java-5.1.49.jar
            |   └── ......
            ├── hive-site.xml
            ├── fair-scheduler.xml
            └── Dockerfile
```

执行以下命令构建镜像

```
docker build -t hadoop-base .
```

---

### Docker-Compose 🖇️

Docker-compose 用于集中管理容器集群

我编写了一个 `compose.yaml` 来设置 HDFS 环境

- 在使用 docker-compose 之前，请确保镜像已成功构建

- 使用 docker-compose 命令启动容器集群

    ```
    docker-compose up -d
    ```

- 执行命令 `docker ps -a` 检查所有容器是否正在运行

#### 启动 HDFS

1. 使用 docker 命令进入 `nn` 容器终端

    ```
    docker exec -it nn su hadoop
    ```

2. 初始化 HDFS 配置
    
    ```
    hdfs namenode -format
    ```

3. 启动 HDFS

    ```
    start-dfs.sh
    ```

    修改配置前停止 HDFS

    ```
    stop-dfs.sh
    ```

#### 启动 Hive

1. 在 `$HIVE_HOME/bin` 中初始化 Hive 配置

    ```
    schematool -dbType mysql -initSchema
    ```
2. 启动 Metastore

    ```
    nohup hive --service metastore &
    ```

    停止 Metastore

    ```
    pkill -f "hive --service metastore"
    ```

3. 启动 Hiveserver2

    ```
    nohup hive --service hiveserver2 &
    ```

    停止 Hiveserver2

    ```
    pkill -f "hive --service hiveserver2"
    ```

4. 进入 Hive JDBC(Beeline) CLI

    ```
    beeline -u "jdbc:hive2://localhost:10000/default" -n hadoop
    ```

#### 启动 YARN

基于8GB内存的测试环境处理10GB以内的数据量，HDFS集群内存分配最佳方案：<br>
每个NodeManager可用内存：1.2GB <br>
每个NodeManager的vCPU数量：1 <br>
每个Map任务内存：762MB <br>
每个Reduce任务内存：512MB <br>
Application Master内存：1GB <br>
数据副本数量：2 <br>

```
宿主机8GB
├─ 系统保留: 2GB
└─ 可用：6GB
   ├─ dn1: 1.2GB (YARN + DataNode)
   ├─ dn2: 1.2GB (YARN + DataNode)
   ├─ dn3: 1.2GB (YARN + DataNode)
   └─ dn4: 1.2GB (YARN + DataNode)
```

1. 启动 YARN
    ```
    start-yarn.sh
    ```

2. 检查 ResourceManager 状态

    ```
    yarn node -list
    ```
    
### 连接测试

- 访问 Flask 前端

    ```
    curl http://localhost:5000
    ```

- 测试 WebHDFS 连接

    ```
    curl http://localhost:5000/test-webhdfs
    ```

    预期响应：`status: success`

- 测试 RPC 连接

    ```
    curl http://localhost:5000/test-rpc
    ```

    预期响应：`status: success`

- 测试 DNS 解析

    ```
    curl http://localhost:5000/test-dns
    ```

    预期响应：NameNode 容器 IP 地址（例如 `172.20.1.10`）

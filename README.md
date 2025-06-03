# Docker Hadoop Container

[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)

## 使用指南

### Flask Container

1. 将 `flask` 目录拉取到本地后，直接在 `flask` 目录内执行以下命令构建镜像：
    ```bash
    docker build -t flask .
    ```

2. 使用 `docker images` 查看镜像是否构建成功：
    ```bash
    root@diakko-virtual-machine:~/docker-hadoop-container/hdfs_single# docker images
    REPOSITORY      TAG       IMAGE ID       CREATED        SIZE
    flask           latest    5303cfbf82fb   35 hours ago   233MB
    ```

#### Flask 目录结构
```
flask/
├── app.py
├── templates/
│   └── index.html
├── requirements.txt
└── Dockerfile
```

---

### HDFS-Single Container

1. HDFS 容器的搭建参考了 [菜鸟教程 Hadoop 教程](https://www.runoob.com/w3cnote/hadoop-tutorial.html)。由于教程中搭建的容器大小高达 6.2GB，因此编写了一个 `Dockerfile` 集成全部依赖环境和步骤。减少存储空间占用的同时也更加方便部署。

2. 使用的 Hadoop 版本为 **3.3.6**，可从以下地址下载：
    [Hadoop 3.3.6 下载地址](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)

3. 将 `hdfs_single` 目录拉取到本地后，将 Hadoop 3.3.6 二进制文件解压并放置在 `hdfs_single` 目录下。

4. 在 `Dockerfile` 文件中直接将 Hadoop 二进制文件复制到 `/usr/local/hadoop`，以便简化操作并缩减镜像大小：
    ```Dockerfile
    COPY hadoop-3.3.6 /usr/local/hadoop
    ```

5. 执行以下命令构建镜像：
    ```bash
    docker build -t hdfs-single .
    ```

6. 使用 `docker images` 查看镜像是否构建成功：
    ```bash
    root@diakko-virtual-machine:~/docker-hadoop-container/hdfs_single# docker images
    REPOSITORY      TAG       IMAGE ID       CREATED        SIZE
    hdfs-single     latest    37c293abd1a7   3 hours ago    3.97GB
    ```
    目前可见对比原来的6.2GB，经过Dockfile编排后存储空间优化了64%，
    还有可优化的空间。





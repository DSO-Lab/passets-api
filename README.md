# Passets 被动资产识别引擎 API 接口模块

本模块以 REST API 的形式根据用户需求向用户返回所需数据。

## 运行环境

- JDK/JRE 1.8

## 容器构建

```
# X86_64
docker build passets-api:<tag> .
# ARMv7
docker build -f Dockerfile_armv7 passets-api:<tag> .
```

## 容器启动

### 使用 docker 启动
```
docker run -it --rm -e ELASTICSEARCH_URL=http://x.x.x.x:9200 -e ELASTICSEARCH_INDEX=passets-logstash -e WAIT_SERVICE=http://x.x.x.x:9200 -p 8081:8080 passets-api:<tag>
```

###  使用 docker-compose 启动

docker-compose.yml
```
version: "3"

services:
  api:
    build:
      context: .
    image: passets-api:<tag>
    container_name: passets-api
    environment:
      - TZ=Asia/Shanghai
      - ELASTICSEARCH_URL=http://<elasticsearch-host>:9200    # ES地址
      - ELASTICSEARCH_INDEX=passets-syslog                    # ES索引
      - WAIT_SERVICE=<elasticsearch-host>:9200                # 启动前要等待的服务
    ports:
      - "8081:8080"
```

构建:
```
docker-compose build
```

启动：
```
docker-compose up -d
```

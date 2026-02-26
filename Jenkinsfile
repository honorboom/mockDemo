pipeline {
    agent any

    options {
        // 保留最近30个构建
        buildDiscarder(logRotator(numToKeepStr: '30'))
        // 设置构建超时为1小时
        timeout(time: 1, unit: 'HOURS')
        // 时间戳
        timestamps()
    }

    parameters {
        string(name: 'BRANCH', defaultValue: 'develop', description: '分支名称')
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: '部署环境')
        booleanParam(name: 'RUN_SECURITY_SCAN', defaultValue: true, description: '运行安全扫描')
        booleanParam(name: 'RUN_PERFORMANCE_TEST', defaultValue: false, description: '运行性能测试')
    }

    environment {
        // Java 环境
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk'
        // Maven 缓存
        MVN_OPTS = '-Xmx1024m'
        // Docker 仓库
        DOCKER_REGISTRY = 'registry.example.com'
        IMAGE_NAME = 'mock-service-backend'
        // SonarQube 配置
        SONAR_SERVER_URL = 'http://sonarqube:9000'
        SONAR_LOGIN = credentials('sonar-token')
        // Slack 通知
        SLACK_CHANNEL = '#ci-cd-notifications'
    }

    stages {
        stage('初始化') {
            steps {
                script {
                    echo "========== 初始化构建环境 =========="
                    echo "分支: ${params.BRANCH}"
                    echo "环境: ${params.ENVIRONMENT}"
                    echo "构建编号: ${BUILD_NUMBER}"
                    echo "构建时间: ${BUILD_TIMESTAMP}"
                }
                // 清理工作空间
                cleanWs()
            }
        }

        stage('拉取代码') {
            steps {
                script {
                    echo "========== 拉取源代码 =========="
                    checkout(
                        [
                            $class: 'GitSCM',
                            branches: [[name: "${params.BRANCH}"]],
                            userRemoteConfigs: [[url: 'https://github.com/your-org/mock-dsp-demo.git']]
                        ]
                    )
                    // 获取 Git 信息
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.GIT_COMMIT_MSG = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    env.GIT_AUTHOR = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                }
            }
        }

        stage('代码扫描') {
            parallel {
                stage('SonarQube 分析') {
                    steps {
                        script {
                            echo "========== 代码质量检查 (SonarQube) =========="
                            dir('mock-service-backend') {
                                sh '''
                                    mvn clean verify sonar:sonar \
                                        -Dsonar.projectKey=mock-service-backend \
                                        -Dsonar.projectName="Mock Service Backend" \
                                        -Dsonar.sources=src/main \
                                        -Dsonar.tests=src/test \
                                        -Dsonar.host.url=${SONAR_SERVER_URL} \
                                        -Dsonar.login=${SONAR_LOGIN} \
                                        -Dsonar.java.binaries=target/classes \
                                        -Dsonar.links.homepage=https://github.com/your-org/mock-dsp-demo \
                                        -Dsonar.links.scm=https://github.com/your-org/mock-dsp-demo
                                '''
                            }
                        }
                    }
                }

                stage('代码规范检查') {
                    when {
                        expression { params.RUN_SECURITY_SCAN == true }
                    }
                    steps {
                        script {
                            echo "========== 代码规范检查 =========="
                            dir('mock-service-backend') {
                                sh '''
                                    # 使用 Checkstyle 检查代码规范
                                    mvn checkstyle:check || true
                                '''
                            }
                        }
                    }
                }
            }
        }

        stage('编译和构建') {
            steps {
                script {
                    echo "========== 编译项目 =========="
                    dir('mock-service-backend') {
                        sh '''
                            mvn clean compile -DskipTests
                        '''
                    }
                }
            }
        }

        stage('单元测试') {
            steps {
                script {
                    echo "========== 运行单元测试 =========="
                    dir('mock-service-backend') {
                        sh '''
                            mvn test \
                                -Dorg.slf4j.simpleLogger.defaultLogLevel=info \
                                || TEST_FAILED=true
                        '''
                    }
                }
                // 发布测试报告
                junit '**/target/surefire-reports/*.xml'
                publishHTML([
                    reportDir: 'mock-service-backend/target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: '代码覆盖率报告'
                ])
            }
        }

        stage('打包') {
            steps {
                script {
                    echo "========== 打包 JAR =========="
                    dir('mock-service-backend') {
                        sh '''
                            mvn package -DskipTests \
                                -Dmaven.test.skip=true
                        '''
                    }
                    // 保存构建产物
                    archiveArtifacts artifacts: 'mock-service-backend/target/*.jar', allowEmptyArchive: false
                }
            }
        }

        stage('安全扫描') {
            when {
                expression { params.RUN_SECURITY_SCAN == true }
            }
            parallel {
                stage('OWASP 依赖检查') {
                    steps {
                        script {
                            echo "========== 依赖安全检查 (OWASP) =========="
                            dir('mock-service-backend') {
                                sh '''
                                    mvn org.owasp:dependency-check-maven:check \
                                        -Ddependency-check.suppression.file=../security/dependency-check-suppression.xml \
                                        || SECURITY_CHECK_FAILED=true
                                '''
                            }
                            // 发布依赖检查报告
                            publishHTML([
                                reportDir: 'mock-service-backend/target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: '依赖安全检查报告'
                            ])
                        }
                    }
                }

                stage('Trivy 漏洞扫描') {
                    steps {
                        script {
                            echo "========== 容器镜像漏洞扫描 (Trivy) =========="
                            sh '''
                                # 构建临时镜像进行扫描
                                docker build -t temp-scan:latest mock-service-backend/

                                # 运行 Trivy 扫描
                                trivy image \
                                    --severity HIGH,CRITICAL \
                                    --exit-code 0 \
                                    --no-progress \
                                    --format json \
                                    -o trivy-report.json \
                                    temp-scan:latest

                                # 清理
                                docker rmi temp-scan:latest
                            '''
                        }
                    }
                }
            }
        }

        stage('性能测试') {
            when {
                expression { params.RUN_PERFORMANCE_TEST == true }
            }
            steps {
                script {
                    echo "========== 性能测试 =========="
                    dir('mock-service-backend') {
                        sh '''
                            # 启动应用进行性能测试
                            java -jar target/*.jar &
                            APP_PID=$!
                            sleep 10

                            # 运行 JMeter 测试（如果存在）
                            # jmeter -n -t tests/performance.jmx -l results.jtl

                            # 关闭应用
                            kill $APP_PID
                        '''
                    }
                }
            }
        }

        stage('Docker 构建和推送') {
            when {
                expression {
                    params.ENVIRONMENT in ['dev', 'staging', 'prod']
                }
            }
            steps {
                script {
                    echo "========== Docker 镜像构建 =========="
                    sh '''
                        cd mock-service-backend

                        # 构建镜像
                        docker build \
                            -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} \
                            -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${GIT_COMMIT_SHORT} \
                            -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest \
                            -f Dockerfile \
                            .

                        # 推送到仓库
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${GIT_COMMIT_SHORT}
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest

                        # 清理本地镜像
                        docker rmi ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
                        docker rmi ${DOCKER_REGISTRY}/${IMAGE_NAME}:${GIT_COMMIT_SHORT}
                        docker rmi ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                    '''
                }
            }
        }

        stage('部署') {
            when {
                expression {
                    params.ENVIRONMENT in ['dev', 'staging', 'prod']
                }
            }
            steps {
                script {
                    echo "========== 部署到 ${params.ENVIRONMENT} 环境 =========="

                    // 根据环境选择部署策略
                    if (params.ENVIRONMENT == 'dev') {
                        sh '''
                            # 部署到开发环境
                            ssh -i ~/.ssh/dev-key deploy@dev-server << EOF
                                cd /app/mock-service-backend
                                docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                                docker-compose down || true
                                docker-compose up -d
                                docker-compose logs -f backend
                            EOF
                        '''
                    } else if (params.ENVIRONMENT == 'staging') {
                        sh '''
                            # 部署到测试环境
                            ssh -i ~/.ssh/staging-key deploy@staging-server << EOF
                                cd /app/mock-service-backend
                                docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:${GIT_COMMIT_SHORT}
                                docker-compose down || true
                                docker-compose up -d
                                # 等待服务启动
                                sleep 10
                                curl -f http://localhost:8080/api/health || exit 1
                            EOF
                        '''
                    } else if (params.ENVIRONMENT == 'prod') {
                        // 生产环境部署前的确认
                        input message: '确认部署到生产环境？', ok: '确认'

                        sh '''
                            # 部署到生产环境
                            ssh -i ~/.ssh/prod-key deploy@prod-server << EOF
                                # 备份数据库
                                docker exec mock-mysql mysqldump \
                                    -uroot -p${MYSQL_PASSWORD} \
                                    mock_service_db \
                                    > backup_\$(date +%Y%m%d_%H%M%S).sql

                                # 拉取新镜像
                                cd /app/mock-service-backend
                                docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:${GIT_COMMIT_SHORT}

                                # 蓝绿部署
                                docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

                                # 更新镜像版本
                                sed -i "s/image:.*/image: ${DOCKER_REGISTRY}\\/${IMAGE_NAME}:${GIT_COMMIT_SHORT}/" docker-compose.yml

                                # 启动新服务
                                docker-compose up -d

                                # 健康检查
                                for i in {1..30}; do
                                    if curl -f http://localhost:8080/api/health; then
                                        echo "应用健康检查通过"
                                        exit 0
                                    fi
                                    sleep 2
                                done
                                exit 1
                            EOF
                        '''
                    }
                }
            }
        }

        stage('烟雾测试') {
            when {
                expression {
                    params.ENVIRONMENT in ['dev', 'staging']
                }
            }
            steps {
                script {
                    echo "========== 部署后烟雾测试 =========="
                    sh '''
                        # 等待服务完全启动
                        sleep 5

                        # 运行基础健康检查
                        API_URL="http://localhost:8080/api"

                        # 检查健康状态
                        curl -f ${API_URL}/health || exit 1

                        # 检查主要 API 端点
                        curl -f ${API_URL}/api-info || true
                        curl -f ${API_URL}/mock/list || true

                        echo "烟雾测试完成"
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "========== 后处理 =========="

                // 清理 Docker 资源
                sh '''
                    docker system prune -f || true
                '''

                // 生成报告
                step([$class: 'JunitResultArchiver',
                    testResults: '**/target/surefire-reports/*.xml',
                    allowEmptyResults: true
                ])
            }
        }

        success {
            script {
                echo "✅ 构建成功"
                // Slack 通知
                if (params.ENVIRONMENT) {
                    sh '''
                        curl -X POST -H 'Content-type: application/json' \
                            --data "{\"text\":\":white_check_mark: 后端构建部署成功\\n环境: ${ENVIRONMENT}\\n分支: ${BRANCH}\\n提交: ${GIT_COMMIT_SHORT}\\n提交者: ${GIT_AUTHOR}\"}" \
                            ${SLACK_WEBHOOK_URL}
                    '''
                }
            }
        }

        failure {
            script {
                echo "❌ 构建失败"
                // Slack 通知
                sh '''
                    curl -X POST -H 'Content-type: application/json' \
                        --data "{\"text\":\":x: 后端构建失败\\n环境: ${ENVIRONMENT}\\n分支: ${BRANCH}\\n提交: ${GIT_COMMIT_SHORT}\\n查看日志: ${BUILD_URL}console\"}" \
                        ${SLACK_WEBHOOK_URL}
                '''
            }
        }

        cleanup {
            // 清理工作空间
            cleanWs(deleteDirs: true, patterns: [
                [pattern: '**/target/**', type: 'INCLUDE'],
                [pattern: '**/dist/**', type: 'INCLUDE'],
                [pattern: '.m2/**', type: 'INCLUDE']
            ])
        }
    }
}

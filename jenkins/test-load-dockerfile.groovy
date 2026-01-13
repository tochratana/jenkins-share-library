@Library("telegrame_notification_share_library@main") _
pipeline {
    agent any

    tools {
        jdk 'jdk-21'                 
        nodejs 'Node-20'        
    }

    environment {
        // Telegram
        CHAT_ID    = "1177908131"
        CHAT_TOKEN = "7873147150:AAGVJ-bpejW4O0XS9FhLQmwEr5Wk-VK89-Y"

        // SonarQube
        SONAR_SCANNER = tool 'sonarqube-scanner'
    }

    stages {

        stage('Clone Repositories') {
            steps {
                dir('prodstack') {
                    git url: 'https://github.com/tochratana/prodstack.git', branch: 'main'
                }
                dir('prodstack-ui') {
                    git url: 'https://github.com/tochratana/prodstack-ui.git', branch: 'main'
                }
            }
        }

        stage('Build Backend (Gradle)') {
            steps {
                dir('prodstack') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean build -x test --no-daemon
                    '''
                }
            }
        }

        stage('SonarQube Scan - Backend') {
            steps {
                dir('prodstack') {
                    withSonarQubeEnv(
                        credentialsId: 'SONARQUBE-TOKEN',
                        installationName: 'sonarqube-scanner'
                    ) {
                        sh """
                            ${SONAR_SCANNER}/bin/sonar-scanner \
                            -Dsonar.projectKey=prodstack-backend \
                            -Dsonar.projectName=prodstack-backend \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.java.binaries=build/classes/java/main \
                            -Dsonar.exclusions=**/test/**,**/build/**
                        """
                    }
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('prodstack-ui') {
                    sh '''
                        npm install
                        npm run build
                    '''
                }
            }
        }

        stage('SonarQube Scan - Frontend') {
            steps {
                dir('prodstack-ui') {
                    withSonarQubeEnv(
                        credentialsId: 'SONARQUBE-TOKEN',
                        installationName: 'sonarqube-scanner'
                    ) {
                        sh """
                            ${SONAR_SCANNER}/bin/sonar-scanner \
                            -Dsonar.projectKey=prodstack-frontend \
                            -Dsonar.projectName=prodstack-frontend \
                            -Dsonar.sources=src \
                            -Dsonar.exclusions=node_modules/**,dist/**,build/**
                        """
                    }
                }
            }
        }

        stage('Quality Gate - Backend') {
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            sendTelegrameMessage(
                                "❌ Backend Quality Gate Failed: ${qg.status}",
                                CHAT_TOKEN,
                                CHAT_ID
                            )
                            error "Quality Gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Load Dockerfiles from Shared Library') {
            steps {
                writeFile file: 'Dockerfile.backend', text: libraryResource('spring/dev.Dockerfile')
                writeFile file: 'Dockerfile.frontend', text: libraryResource('reactjs/dev.Dockerfile')
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build -t backend-app:latest   -f Dockerfile.backend ./prodstack
                    docker build -t frontend-app:latest  -f Dockerfile.frontend ./prodstack-ui
                    docker build -t postgres-db:latest   -f Dockerfile.db .
                '''
            }
        }

        stage('Run Containers') {
            steps {
                sh '''
                    docker network create prod-net || true

                    docker rm -f postgres backend frontend || true

                    docker run -d --name postgres \
                      --network prod-net \
                      -e POSTGRES_PASSWORD=postgres \
                      -p 5432:5432 postgres-db:latest

                    docker run -d --name backend \
                      --network prod-net \
                      -p 8080:8080 backend-app:latest

                    docker run -d --name frontend \
                      --network prod-net \
                      -p 3000:3000 frontend-app:latest
                '''
            }
        }

        stage('Send Telegram Success') {
            steps {
                script {
                    sendTelegrameMessage(
                        """✅ Deployment Successful
                        Frontend: http://yourhost:3000
                        Backend: http://yourhost:8080
                        Quality Gate: PASSED""",
                        CHAT_TOKEN,
                        CHAT_ID
                    )
                }
            }
        }
    }

    post {
        failure {
            script {
                sendTelegrameMessage(
                    "❌ Deployment failed. Check Jenkins logs.",
                    CHAT_TOKEN,
                    CHAT_ID
                )
            }
        }
    }
}

pipeline {
    agent {
        docker {
            image 'adoptopenjdk/openjdk11:jdk-11.0.8_10-alpine'
        }
    }
    stages {
        stage("Presteps") {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew shadowJar'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Verify') {
            steps {
                sh './gradlew spotbugsMain'
                sh './gradlew dependencyCheckAnalyze'
            }
        }
        stage ('Deploy') {
            steps{
                script {
                    def filePath = sh(script: 'ls build/libs/StonksBot*.jar', returnStdout: true)
                    def remote = [:]
                    remote.name = "stonksbot-instance"
                    remote.host = "172.31.21.76"
                    remote.allowAnyHosts = true
                    withCredentials([sshUserPrivateKey(credentialsId: 'bot-instance-key', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName')]) {
                        remote.user = userName
                        remote.identityFile = identity
                        sshPut remote: remote, from: 'build/libs/StonksBot-1.0-SNAPSHOT-all.jar', into: '/opt/stonksbot/stonksbot.jar', override: true
                        sshCommand remote: remote, command: 'sudo systemctl restart stonksbot'
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/**/*.jar,build/reports/**/*.html', fingerprint: true
            junit 'build/reports/**/*.xml'
        }
    }
}


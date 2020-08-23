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
                sshagent(credentials : ['bot-instance-key']) {
                    sh 'find . -name *.jar -exec scp {} stonksbot@172.31.21.76:/opt/stonksbot/stonksbot.jar \\;'
                    sh 'ssh stonksbot@172.31.21.76 sudo systemctl restart stonksbot'
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


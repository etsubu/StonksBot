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
                sh './gradlew build'
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
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
            junit 'build/reports/**/*.xml,build/reports/**/*.html'
        }
    }
}


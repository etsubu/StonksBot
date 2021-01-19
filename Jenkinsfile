pipeline {
    agent any
    /* Run each day at 2AM */
    triggers { cron(env.BRANCH_NAME == "master" ? "H 2 * * *" : "") }

    options { buildDiscarder(logRotator(numToKeepStr: '5')) }

    environment {
        AWS_ACCESS_KEY_ID     = credentials('AWS_CREDENTIAL_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_CREDENTIAL_SECRET')
    }

    stages {
        stage("Presteps") {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean'
                echo "Running on branch ${env.BRANCH_NAME}"
            }
        }
        stage('Build') {
            steps {
                sh './gradlew shadowJar -x test'
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
            }
        }
        stage ('Deploy') {
            when {
                branch 'master'
            }
            steps{
                echo 'Deploy'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/**/*.jar,build/reports/**/*.html', fingerprint: true
            junit 'build/reports/**/*.xml'
        }
        cleanup {
            cleanWs()
        }
    }
}


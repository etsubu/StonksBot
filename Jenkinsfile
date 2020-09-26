pipeline {
    agent {
        docker {
            image 'adoptopenjdk/openjdk11:jdk-11.0.8_10-alpine'
            args '-v $HOME/.gradle:/root/.gradle'
        }
    }
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
                script {
                    def filePath = sh(script: 'ls build/libs/StonksBot*-all.jar', returnStdout: true)
                    def remote = [:]
                    remote.name = "stonksbot-instance"
                    remote.host = sh(script: "aws ec2 describe-instances --filters Name=tag-value,Values=stonksbot --query 'Reservations[*].Instances[*].{ip:PrivateIpAddress}' --output text", returnStdout: true)
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
        cleanup {
            cleanWs()
        }
    }
}


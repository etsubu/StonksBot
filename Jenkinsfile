pipeline {
    agent any
    /* Run each day at 2AM */
    triggers { cron(env.BRANCH_NAME == "master" ? "H 2 * * *" : "") }

    options { buildDiscarder(logRotator(numToKeepStr: '5')) }

    environment {
        AWS_ACCESS_KEY_ID     = credentials('aws_ec2_deployer_id')
        AWS_SECRET_ACCESS_KEY = credentials('aws_ec2_deployer_secret')
        OATH1 = credentials('TESTER_BOT_OATH')
        OATH2 = credentials('STONKSBOT_OATH')
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
        stage('Integration test') {
            steps {
                sh './gradlew integrationTest'
            }
        }
        stage ('Deploy') {
            when {
                branch 'master'
            }
            steps{
                /* Persist .jar file to s3 bucket */
                sh 'aws s3 cp $(ls build/libs/*-all.jar) s3://stonksbot/stonksbot.jar'
                /* Restart the bot on ec2 instance. Startup handles syncing .jar file from s3 */
                script {
                    def remote = [:]
                    remote.name = "stonksbot-instance"
                    remote.host = sh(script: "aws ec2 describe-instances --region=eu-west-1 --filters Name=tag-value,Values=stonksbot --query 'Reservations[*].Instances[*].PublicIpAddress' --output=text |tr -d '\n'", returnStdout: true)
                    remote.allowAnyHosts = true
                    withCredentials([sshUserPrivateKey(credentialsId: 'stonksbot_ssh_key', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName')]) {
                        remote.user = userName
                        remote.identityFile = identity
                        sshCommand remote: remote, command: 'sudo systemctl restart stonksbot'
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/**/*.jar,build/reports/**/*.html,build/reports/**/*.xml', fingerprint: true
            junit 'build/reports/**/*.xml'
        }
        cleanup {
            cleanWs()
        }
    }
}


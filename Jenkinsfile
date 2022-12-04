library 'jenkins-pipeline-library@master' _

pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    buildPlugin()
                }
            }
        }

        stage('Create Directories') {
            steps {
                sh 'mkdir -p deployment/plugins/KnockturnCore/modules/'
            }
        }

        stage('Move') {
            steps {
                sh 'cp ./build/libs/housepoints-*.jar ./deployment/plugins/HousePoints.jar'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    deployPlugin(["hogwarts", "towny"])
                }
            }
        }
    }

    post {
        always {
            discordMessage()
        }
    }
}
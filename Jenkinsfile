pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                sh 'chmod +x gradlew'
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'
                sh './gradlew build -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD --stacktrace'
            }
        }
        stage('Test') {
            steps {
                echo 'Test..'
                sh './gradlew test -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD --stacktrace'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying..'
                sh './gradlew artifactoryPublish -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD --stacktrace'
            }
        }
    }
}
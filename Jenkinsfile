pipeline {
    agent any

    tools {
       jdk "jdk-13"
    }

    stages {
        stage('Prepare') {
            steps {
                sh 'chmod +x gradlew'
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'
                sh './gradlew build -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD -Psigning_key_ring_file=$SIGNING_KEY_RING_FILE -Psigning_key_ring_file_password=$SIGNING_KEY_RING_FILE_PASSWORD -PossrhUser=$OSSRH_USER -PossrhPassword=$OSSRH_PASSWORD --stacktrace'
            }
        }
        stage('Test') {
            steps {
                echo 'Test..'
                sh './gradlew test -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD -Psigning_key_ring_file=$SIGNING_KEY_RING_FILE -Psigning_key_ring_file_password=$SIGNING_KEY_RING_FILE_PASSWORD  -PossrhUser=$OSSRH_USER -PossrhPassword=$OSSRH_PASSWORD --stacktrace'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying..'
                sh './gradlew artifactoryPublish -Partifactory_user=$ARTIFACTORY_USER -Partifactory_password=$ARTIFACTORY_PASSWORD -Psigning_key_ring_file=$SIGNING_KEY_RING_FILE -Psigning_key_ring_file_password=$SIGNING_KEY_RING_FILE_PASSWORD -PossrhUser=$OSSRH_USER -PossrhPassword=$OSSRH_PASSWORD --stacktrace'
            }
        }
    }
}
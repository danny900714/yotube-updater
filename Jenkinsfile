pipeline {
    agent {
        kubernetes {
            containerTemplate {
                name 'android'
                image 'openjdk:8u232-stretch'
                ttyEnabled true
            }
        }
    }
    environment {
        ANDROID_HOME = '/opt/android-linux-sdk'
    }
    stages {
        stage ('Pre-Build') {
            steps {
                sh 'mkdir -p /tmp/android-linux-sdk'
                sh 'wget -P /tmp/android-linux-sdk/ https://dl.google.com/android/repository/platform-29_r03.zip'
                sh 'mkdir -p /opt/android-linux-sdk'
                sh 'unzip /tmp/android-linux-sdk/platform-29_r03.zip -d /opt/android-linux-sdk'
                sh './gradlew clean assembleBetaDebug'
            }
        }
        stage ('Build') {
            steps {
                sh './gradlew build'
            }
        }
    }
}
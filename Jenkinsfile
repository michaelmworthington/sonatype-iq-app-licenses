pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''echo "PATH = ${PATH}"
echo "M2_HOME = ${M2_HOME}"
           '''
      }
    }
    stage('Build') {
      steps {
        sh '''cd sonatype-iq-app-licenses
mvn clean package'''
      }
    }
    stage('Policy Evaluation') {
      steps {
        nexusPolicyEvaluation(iqStage: 'build', iqApplication: 'sonatype-iq-app-licenses')
      }
    }
  }
  tools {
    maven 'Maven 3.3.9 - Auto Install'
    jdk 'JDK 8u66 - Auto Install'
  }
}
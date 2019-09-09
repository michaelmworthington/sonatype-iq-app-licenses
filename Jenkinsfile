pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args '-v /root/.m2:/root/.m2'
    }

  }
  stages {
    stage('Prep') {
      steps {
        sh 'echo "PATH = ${PATH}"'
        sh 'echo "M2_HOME = ${M2_HOME}"'
        sh 'pwd'
        sh 'ls -la'
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Test'){
      steps {
        sh 'mvn test'
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }
      }
    }
    stage('Policy Evaluation') {
      steps {
        nexusPolicyEvaluation iqStage: 'build',
                              iqApplication: selectedApplication('sonatype-iq-app-licenses')
      }
    }
  }
}
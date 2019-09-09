pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args '-v /Users/mworthington/.m2:/root/.m2'
    }

  }
  stages {
    stage('Prep') {
      steps {
        sh 'echo "PATH = ${PATH}"'
        sh 'echo "M2_HOME = ${M2_HOME}"'
        sh 'pwd'
        sh 'ls -la'
        sh 'id'
        sh 'cat ~/.m2/settings.xml'
      }
    }
    stage('Build') {
      steps {
        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {
          sh 'mvn -B -s $MAVEN_SETTINGS clean install'
        }
      }
    }
    stage('Test'){
      steps {
        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {
          sh 'mvn -B -s $MAVEN_SETTINGS test'
        }
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
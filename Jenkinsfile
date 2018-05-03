pipeline {
  agent any
  tools { 
    maven 'Maven 3.3.9 - Auto Install' 
    jdk 'JDK 8u66 - Auto Install' 
  }
  stages {
    stage ('Initialize') {
      steps {
        sh '''echo "PATH = ${PATH}"
              echo "M2_HOME = ${M2_HOME}"
           ''' 
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }
  }
}

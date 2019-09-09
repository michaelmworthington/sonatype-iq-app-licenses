//tools {
//  maven 'Maven 3.3.9 - Auto Install'
//  jdk 'JDK 8u66 - Auto Install'
//}

timestamps {
  node() {
    stage('Initialize') {
        sh '''echo "PATH = ${PATH}"
echo "M2_HOME = ${M2_HOME}"
           '''
    }
    stage('Build') {
        sh '''cd sonatype-iq-app-licenses
mvn clean install'''
    }
    stage('Policy Evaluation') {
        nexusPolicyEvaluation iqStage: 'build',
                              iqApplication: selectedApplication('sonatype-iq-app-licenses')

    }
  }
}
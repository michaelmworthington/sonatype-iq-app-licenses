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
mvn clean install'''
      }
    }
    stage('Policy Evaluation') {
      steps {
        def result = nexusPolicyEvaluation(iqStage: 'build', iqApplication: 'sonatype-iq-app-licenses')

        nexusPolicyResultNotifier
          bitbucketNotification:
            nexusBitbucketNotification(sendBitbucketNotification: false,
              commitHash: commitHash,
              projectKey: 'NND',
              repositorySlug: 'sample-application')
          jiraNotification:
            nexusJiraNotification(sendJiraNotification: false,
              projectKey: 'DP')

        //nexusPolicyResultNotifier bitbucketNotification: nexusBitbucketNotification(commitHash: 'hash', jobCredentialsId: '', projectKey: 'DP', repositorySlug: 'abc', sendBitbucketNotification: true)

      }
    }
  }
  tools {
    maven 'Maven 3.3.9 - Auto Install'
    jdk 'JDK 8u66 - Auto Install'
  }
}
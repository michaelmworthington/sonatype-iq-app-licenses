def MAVEN_PROFILE = ""
//com.sonatype.nexus.api.iq.ApplicationPolicyEvaluation
def npe

pipeline {
  agent any
//  agent {
//    docker {
//      image 'maven:3-alpine'
//      args '-v /Users/mworthington/.m2:/root/.m2'
//    }
//  }

  tools {
    maven 'Maven 3.5.3 - Local Install'
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '4'))
  }

  parameters {
    booleanParam (name: 'IS_RELEASE', defaultValue: true, description: 'Is this a release build (true/false)?')
    booleanParam (name: 'BE_NICE_AND_CLEAN_UP', defaultValue: false, description: 'Cleanup the transient jars that are now in the docker image?')
    booleanParam (name: 'MANUAL_TEST_FAIL', defaultValue: false, description: 'Did manual testing succeed?')
  }

  environment {
    POM_VERSION = readMavenPom().getVersion()
    POM_RELEASE_VERSION = POM_VERSION.minus("-SNAPSHOT")
    NXRM_TAG_NAME="sonatype-iq-app-licenses.staging-demo.${BUILD_NUMBER}"
  }

  stages {
    stage('Prep') {
      steps {
        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {
          echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
          echo "For Maven Version: ${POM_VERSION} and Release Version: ${POM_RELEASE_VERSION} and is release build: ${params.IS_RELEASE} with tag: ${NXRM_TAG_NAME}"


          sh 'env'
          sh 'echo "Build Number = ${BUILD_ID}"'

          sh 'echo "PATH = ${PATH}"'
          sh 'echo "M2_HOME = ${M2_HOME}"'
          sh 'pwd'
          sh 'ls -la'
          sh 'id'
          //sh 'cat $HOME/.m2/settings.xml'
          //sh 'mvn -B -s $MAVEN_SETTINGS help:effective-settings'
          //sh 'mvn -B -s $MAVEN_SETTINGS help:effective-pom'
        }
      }
    }
    stage('Prepare Maven Release Version') {
      when {
        equals expected: true, actual: params.IS_RELEASE
      }
      steps {
        script {
          MAVEN_PROFILE="-P staging-release"

          try
          {
            //http://localhost:8052/env-vars.html/
            echo "JOB_BASE_NAME: ${JOB_BASE_NAME}"

            echo "BRANCH_NAME: ${BRANCH_NAME}"
            echo "GIT_COMMIT: ${GIT_COMMIT}"
            echo "GIT_BRANCH: ${GIT_BRANCH}"
            echo "GIT_URL: ${GIT_URL}"
          }
          catch (Exception e)
          {
            BRANCH_NAME = "local-branch"
            GIT_COMMIT = "N/A"
            GIT_BRANCH = "N/A"
            GIT_URL = "N/A"
          }
        }

        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {

          sh 'mvn -B -s $MAVEN_SETTINGS versions:set -DnewVersion=${POM_RELEASE_VERSION}-${BUILD_NUMBER}'

          createTag nexusInstanceId: 'nexus3-demo',
                    tagName: "${NXRM_TAG_NAME}",
                    tagAttributesJson: '{' +
                            '"user" : "' + "${USER}" + '",' +
                            '"origPomVersion" : "' + "${POM_VERSION}" + '",' +
                            '"releasePomVersion" : "' + "${POM_RELEASE_VERSION}" + '",' +
                            '"branch" : "' + "${BRANCH_NAME}" + '",' +
//                            '"changeId" : "' + "${CHANGE_ID}" + '",' +
//                            '"changeLink" : "' + "${CHANGE_URL}" + '",' +
//                            '"changeTitle" : "' + "${CHANGE_TITLE}" + '",' +
//                            '"changeAuthor" : "' + "${CHANGE_AUTHOR}" + '",' +
                            '"gitCommit" : "' + "${GIT_COMMIT}" + '",' +
                            '"gitBranch" : "' + "${GIT_BRANCH}" + '",' +
                            '"gitLink" : "' + "${GIT_URL}" + '",' +
//                            '"gitCommitter" : "' + "${GIT_COMMITTER_NAME}" + '",' +
//                            '"gitAuthor" : "' + "${GIT_AUTHOR_NAME}" + '",' +
                            '"jenkinsJob": "' + "${BUILD_URL}" +'"}'
        }
      }
    }
    stage('Build Web App') {
      steps {
        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {

          withEnv(["MAVEN_PROFILE=${MAVEN_PROFILE}"]) {
            sh 'mvn -B -s $MAVEN_SETTINGS ${MAVEN_PROFILE} -Dtag=${NXRM_TAG_NAME} clean deploy'
          }
        }
      }
    }
    stage('Quality Tests') {
      parallel {
        stage('JUnit') {
          steps {
            configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {
              sh 'mvn -B -s $MAVEN_SETTINGS test'
            }
          }
          post {
            always {
              junit '**/target/surefire-reports/*.xml'
            }
          }
        }
        stage('IQ OSS Policy Evaluation') {
          steps {
            //TODO: the jenkins plugin is not picking up the git hash
            script {

              npe = nexusPolicyEvaluation iqStage: 'build',
                                              iqApplication: 'sonatype-iq-app-licenses'
            }
          }
        }
        stage('Static Analysis') {
          steps {
            echo '...run SonarQube or other SAST tools here'
          }
        }
        stage('Manual Testing') {
          when {
            //branch 'develop'
            equals expected: true, actual: params.MANUAL_TEST_FAIL
          }
          steps {
            print "MANUAL TESTING FAILED"

            configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {

              withEnv(["MAVEN_PROFILE=${MAVEN_PROFILE}"]) {

                sh 'mvn -B -s $MAVEN_SETTINGS ${MAVEN_PROFILE} nxrm3:staging-delete -Dtag=${NXRM_TAG_NAME}'

                httpRequest acceptType: 'APPLICATION_JSON',
                            authentication: 'nexus-iq-demo',
                            consoleLogResponseBody: true,
                            contentType: 'APPLICATION_JSON',
                            httpMode: 'PUT',
                            responseHandle: 'NONE',
                            url: "http://localhost:8083/nexus/service/rest/v1/tags/${NXRM_TAG_NAME}",
                            validResponseCodes: '200',
                            requestBody: '{ "attributes": {' +
                                    '"user" : "' + "${USER}" + '",' +
                                    '"origPomVersion" : "' + "${POM_VERSION}" + '",' +
                                    '"releasePomVersion" : "' + "${POM_RELEASE_VERSION}" + '",' +
                                    '"branch" : "' + "${BRANCH_NAME}" + '",' +
//                            '"changeId" : "' + "${CHANGE_ID}" + '",' +
//                            '"changeLink" : "' + "${CHANGE_URL}" + '",' +
//                            '"changeTitle" : "' + "${CHANGE_TITLE}" + '",' +
//                            '"changeAuthor" : "' + "${CHANGE_AUTHOR}" + '",' +
                                    '"gitCommit" : "' + "${GIT_COMMIT}" + '",' +
                                    '"gitBranch" : "' + "${GIT_BRANCH}" + '",' +
                                    '"gitLink" : "' + "${GIT_URL}" + '",' +
//                            '"gitCommitter" : "' + "${GIT_COMMITTER_NAME}" + '",' +
//                            '"gitAuthor" : "' + "${GIT_AUTHOR_NAME}" + '",' +
                                    '"jenkinsJob": "' + "${BUILD_URL}" +'",' +
                                    '"testing-status": "' + "FAILED" +'"' +
                                    '} }'
              }
            }

            script {
              //use SUCCESS, FAILURE or ABORTED
              currentBuild.result = 'FAILURE'
              throw new Exception("Throw to stop pipeline")
              // do not use the following, as it does not trigger post steps (i.e. the failure step)
              // error "your reason here"
            }

          }
        }
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker build -t local-mike:19447/michaelmworthington/sonatype-iq-app-licenses:${POM_RELEASE_VERSION}-${BUILD_NUMBER} .'
      }
    }

    stage('Publish Container') {
      when {
        //branch 'develop'
        equals expected: true, actual: params.IS_RELEASE
      }
      steps {
        //input 'Push to Nexus Repo?'

        sh 'docker push local-mike:19447/michaelmworthington/sonatype-iq-app-licenses:${POM_RELEASE_VERSION}-${BUILD_NUMBER}'

        associateTag nexusInstanceId: 'nexus3-demo',
                     search: [[key: 'repository', value: 'docker-hosted-beta'], [key: 'name', value: "michaelmworthington/sonatype-iq-app-licenses"], [key: 'version', value: "${POM_RELEASE_VERSION}-${BUILD_NUMBER}"]],
                     tagName: "${NXRM_TAG_NAME}"

      }
    }

    stage('Test Container') {
      parallel {
        stage('Test Container') {
          steps {
            catchError() {
              echo 'Test Container here'
            }

          }
        }
        stage('IQ-Scan Container') {
          steps {
            sh 'docker save local-mike:19447/michaelmworthington/sonatype-iq-app-licenses:${POM_RELEASE_VERSION}-${BUILD_NUMBER} -o $WORKSPACE/sonatype-iq-app-licenses-docker-image.tar'
            nexusPolicyEvaluation iqStage: 'stage-release',
                                  iqApplication: 'sonatype-iq-app-licenses',
                                  iqScanPatterns: [[scanPattern: 'sonatype-iq-app-licenses-docker-image.tar']]
          }
        }
      }
    }

    stage('Promote Tag') {
      when {
        //branch 'develop'
        equals expected: true, actual: params.IS_RELEASE
      }
      steps {
        //input 'Move Image out of Beta?'

        // TODO: the jenkins plugin doesn't have a source repo, so this conflicts with both maven and docker tagged with the same thing
        //https://help.sonatype.com/integrations/nexus-and-continuous-integration/nexus-platform-plugin-for-jenkins#NexusPlatformPluginforJenkins-Move(Promote)Components
        // moveComponents destination: 'docker-hosted', nexusInstanceId: 'nexus3-demo', tagName: "${NXRM_TAG_NAME}"

        configFileProvider([configFile(fileId: 'c53477b4-afad-4fe1-89e7-b1c28e899e4e', variable: 'MAVEN_SETTINGS')]) {

          withEnv(["MAVEN_PROFILE=${MAVEN_PROFILE}"]) {

            //https://help.sonatype.com/integrations/nexus-and-continuous-integration/repository-manager-for-maven-plugin#RepositoryManagerforMavenPlugin-nxrm3:staging-move
            sh 'mvn -B -s $MAVEN_SETTINGS ${MAVEN_PROFILE} nxrm3:staging-move -Dtag=${NXRM_TAG_NAME} -DsourceRepository=docker-hosted-beta -DdestinationRepository=docker-hosted-qa'

            sh 'mvn -B -s $MAVEN_SETTINGS ${MAVEN_PROFILE} nxrm3:staging-move -Dtag=${NXRM_TAG_NAME} -DsourceRepository=maven-releases-staging -DdestinationRepository=maven-releases-qa'

          }
        }
      }
    }
    stage('Log Tag Attributes') {
      steps {
//        print "NPE: ${npe.getApplicationCompositionReportUrl()}"
//        print "NPE.affected = ${npe.affectedComponentCount}"
//        print "NPE.critical = ${npe.criticalComponentCount}"
//        print "NPE.severe = ${npe.severeComponentCount}"
//        print "NPE.moderate = ${npe.moderateComponentCount}"
//        print "NPE.grandfathered = ${npe.grandfatheredPolicyViolationCount}"
//
//        tagAttributesJson: '{' +
//                '"user" : "' + "${USER}" + '",' +
//                '"origPomVersion" : "' + "${POM_VERSION}" + '",' +
//                '"releasePomVersion" : "' + "${POM_RELEASE_VERSION}" + '",' +
//                '"branch" : "' + "${BRANCH_NAME}" + '",' +
////                            '"changeId" : "' + "${CHANGE_ID}" + '",' +
////                            '"changeLink" : "' + "${CHANGE_URL}" + '",' +
////                            '"changeTitle" : "' + "${CHANGE_TITLE}" + '",' +
////                            '"changeAuthor" : "' + "${CHANGE_AUTHOR}" + '",' +
//                '"gitCommit" : "' + "${GIT_COMMIT}" + '",' +
//                '"gitBranch" : "' + "${GIT_BRANCH}" + '",' +
//                '"gitLink" : "' + "${GIT_URL}" + '",' +
////                            '"gitCommitter" : "' + "${GIT_COMMITTER_NAME}" + '",' +
////                            '"gitAuthor" : "' + "${GIT_AUTHOR_NAME}" + '",' +
//                '"jenkinsJob": "' + "${BUILD_URL}" +'"' +
//                '}'



        httpRequest acceptType: 'APPLICATION_JSON',
                    authentication: 'nexus-iq-demo',
                    consoleLogResponseBody: true,
                    contentType: 'APPLICATION_JSON',
                    httpMode: 'PUT',
                    responseHandle: 'NONE',
                    url: "http://localhost:8083/nexus/service/rest/v1/tags/${NXRM_TAG_NAME}",
                    validResponseCodes: '200',
                    requestBody: '{ "attributes": {' +
                            '"user" : "' + "${USER}" + '",' +
                            '"origPomVersion" : "' + "${POM_VERSION}" + '",' +
                            '"releasePomVersion" : "' + "${POM_RELEASE_VERSION}" + '",' +
                            '"branch" : "' + "${BRANCH_NAME}" + '",' +
//                            '"changeId" : "' + "${CHANGE_ID}" + '",' +
//                            '"changeLink" : "' + "${CHANGE_URL}" + '",' +
//                            '"changeTitle" : "' + "${CHANGE_TITLE}" + '",' +
//                            '"changeAuthor" : "' + "${CHANGE_AUTHOR}" + '",' +
                            '"gitCommit" : "' + "${GIT_COMMIT}" + '",' +
                            '"gitBranch" : "' + "${GIT_BRANCH}" + '",' +
                            '"gitLink" : "' + "${GIT_URL}" + '",' +
//                            '"gitCommitter" : "' + "${GIT_COMMITTER_NAME}" + '",' +
//                            '"gitAuthor" : "' + "${GIT_AUTHOR_NAME}" + '",' +
                            '"jenkinsJob": "' + "${BUILD_URL}" +'",' +
                            '"iqReportURL": "' + "${npe.applicationCompositionReportUrl}" +'",' +
                            '"iqReportAffectedComponents": "' + "${npe.affectedComponentCount}" +'",' +
                            '"iqReportCriticalComponents": "' + "${npe.criticalComponentCount}" +'",' +
                            '"iqReportSevereComponents": "' + "${npe.severeComponentCount}" +'",' +
                            '"iqReportModerateComponents": "' + "${npe.moderateComponentCount}" +'",' +
                            '"iqReportGrandfatheredComponents": "' + "${npe.grandfatheredPolicyViolationCount}" +'"' +
                            '} }'
      }
    }
    stage('Cleanup Tag') {
      when {
        //branch 'develop'
        equals expected: true, actual: params.BE_NICE_AND_CLEAN_UP
      }
      steps {
        // TODO: the jenkins and maven plugins only deletes all the components associated with the tag
        //curl -X POST "http://localhost:8083/nexus/service/rest/v1/staging/delete?repository=maven-releases-qa&maven.artifactId=sonatype-iq-app-licenses&tag=RBCDemoJenkinsfile-56" -H "accept: application/json"

        httpRequest acceptType: 'APPLICATION_JSON',
                    authentication: 'nexus-iq-demo',
                    consoleLogResponseBody: true,
                    contentType: 'APPLICATION_JSON',
                    httpMode: 'PUT',
                    responseHandle: 'NONE',
                    url: "http://localhost:8083/nexus/service/rest/v1/staging/delete?repository=maven-releases-qa&maven.artifactId=sonatype-iq-app-licenses&tag=${NXRM_TAG_NAME}",
                    validResponseCodes: '200'
      }
    }
  }
}
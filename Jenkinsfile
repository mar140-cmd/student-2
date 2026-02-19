pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK25'
    }

    // environment {
    //     SONAR_TOKEN = credentials('squ_41143ed33b4f64794c9a85f58e91da3031a6aa53')
    //     DOCKER_HUB_CREDENTIALS = credentials('dockerhub-credentials')
    //     DOCKER_IMAGE = 'votre-username/students-app-backend'
    //     NEXUS_URL = 'http://localhost:8081'
    //     NEXUS_CREDENTIALS = credentials('nexus-credentials')
    // }

    stages {

        stage('Declarative: Tool Install') {
            steps {
                echo '=== Installation des outils ==='
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Checkout Git repository') {
            steps {
                echo '=== Checkout du code ==='
                checkout scm
            }
        }

        stage('Version Maven') {
            steps {
                echo '=== Version Maven ==='
                sh 'mvn --version'
            }
        }

        stage('Mvn Clean') {
            steps {
                echo '=== Nettoyage ==='
                sh 'mvn clean'
            }
        }

        stage('Maven Compile') {
            steps {
                echo '=== Compilation ==='
                sh 'mvn compile'
            }
        }

        stage('JUnit / Mockito Tests') {
            steps {
                echo '=== Execution des tests ==='
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build package') {
            steps {
                echo '=== Build du package ==='
                sh 'mvn package -DskipTests'
            }
        }

        stage('Maven Install') {
            steps {
                echo '=== Installation Maven ==='
                sh 'mvn install -DskipTests'
            }
        }

        stage('Rapport JaCoCo') {
            steps {
                echo '=== Rapport de couverture JaCoCo ==='
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                }
            }
        }

        stage('JaCoCo coverage report') {
            steps {
                echo '=== Publication rapport couverture ==='
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report'
                ])
            }
        }

        stage('mvn SonarQube') {
            steps {
                echo '=== Analyse SonarQube ==='
                sh """
                    mvn sonar:sonar \
                    -Dsonar.host.url=http://localhost:9005 \
                    -Dsonar.token=${squ_41143ed33b4f64794c9a85f58e91da3031a6aa53}
                """
            }
        }

        stage('Deploy to Nexus') {
            steps {
                echo '=== Deploiement sur Nexus ==='
                sh """
                    mvn deploy \
                    -DskipTests \
                    -DaltDeploymentRepository=nexus::default::${NEXUS_URL}/repository/maven-releases/
                """
            }
        }

        stage('Build Docker Image (Spring Part)') {
            steps {
                echo '=== Build image Docker ==='
                sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                echo '=== Push image sur DockerHub ==='
                sh "echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline termine avec succes !'
            emailext(
                subject: "✅ BUILD SUCCESS - ${JOB_NAME} #${BUILD_NUMBER}",
                body: "Le build ${JOB_NAME} #${BUILD_NUMBER} a reussi.\nVoir: ${BUILD_URL}",
                to: 'votre-email@example.com'
            )
        }
        failure {
            echo '❌ Pipeline echoue !'
            emailext(
                subject: "❌ BUILD FAILED - ${JOB_NAME} #${BUILD_NUMBER}",
                body: "Le build ${JOB_NAME} #${BUILD_NUMBER} a echoue.\nVoir: ${BUILD_URL}",
                to: 'votre-email@example.com'
            )
        }
        always {
            echo '=== Nettoyage workspace ==='
            cleanWs()
        }
    }
}

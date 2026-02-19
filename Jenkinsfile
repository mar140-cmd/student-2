pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK25'
    }

    stages {

        stage('Declarative: Tool Install') {
            steps {
                echo '=== Installation des outils ==='
                bat 'java -version'
                bat 'mvn -version'
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
                bat 'mvn --version'
            }
        }

        stage('Mvn Clean') {
            steps {
                echo '=== Nettoyage ==='
                bat 'mvn clean'
            }
        }

        stage('Maven Compile') {
            steps {
                echo '=== Compilation ==='
                bat 'mvn compile'
            }
        }

        stage('JUnit / Mockito Tests') {
            steps {
                echo '=== Execution des tests ==='
                bat 'mvn test'
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
                bat 'mvn package -DskipTests'
            }
        }

        stage('Maven Install') {
            steps {
                echo '=== Installation Maven ==='
                bat 'mvn install -DskipTests'
            }
        }

        stage('Rapport JaCoCo') {
            steps {
                echo '=== Rapport de couverture JaCoCo ==='
                bat 'mvn jacoco:report'
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
                bat 'mvn sonar:sonar -Dsonar.host.url=http://localhost:9005 -Dsonar.token=squ_41143ed33b4f64794c9a85f58e91da3031a6aa53'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '=== Build image Docker ==='
                bat "docker build -t mariemriahii/students-app-backend:%BUILD_NUMBER% ."
                bat "docker tag mariemriahii/students-app-backend:%BUILD_NUMBER% mariemriahii/students-app-backend:latest"
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                echo '=== Push sur DockerHub ==='
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    bat "docker push mariemriahii/students-app-backend:%BUILD_NUMBER%"
                    bat "docker push mariemriahii/students-app-backend:latest"
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline termine avec succes !'
        }
        failure {
            echo 'Pipeline echoue !'
        }
        always {
            echo '=== Nettoyage workspace ==='
            cleanWs()
        }
    }
}
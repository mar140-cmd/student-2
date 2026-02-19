pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-25'
    }

    environment {
        SONAR_TOKEN = credentials('squ_41143ed33b4f64794c9a85f58e91da3031a6aa53')
    }

    stages {

        stage('Checkout') {
            steps {
                echo '--- Récupération du code source ---'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '--- Compilation du projet ---'
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo '--- Exécution des tests ---'
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Couverture JaCoCo') {
            steps {
                echo '--- Génération du rapport de couverture ---'
                bat 'mvn jacoco:report'
            }
        }

        stage('Analyse SonarQube') {
            steps {
                echo '--- Envoi vers SonarQube ---'
                withSonarQubeEnv('SonarQube-Local') {
                    bat """
                        mvn sonar:sonar ^
                        -Dsonar.projectKey=students-app ^
                        -Dsonar.projectName="Students App" ^
                        -Dsonar.host.url=http://localhost:9005 ^
                        -Dsonar.token=%squ_41143ed33b4f64794c9a85f58e91da3031a6aa53%
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '--- Vérification du Quality Gate ---'
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                echo '--- Packaging JAR ---'
                bat 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline terminé avec succès !'
        }
        failure {
            echo '❌ Pipeline échoué — vérifie les logs.'
        }
        always {
            echo '--- Fin du pipeline ---'
        }
    }
}
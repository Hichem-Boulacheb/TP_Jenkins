pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

    options {
        skipStagesAfterUnstable()
        timestamps()
    }

    stages {

        // ===========================
        stage('Test') {
            steps {
                echo "Running unit tests"
                bat 'gradlew.bat clean test'

                echo "Publishing JUnit results"
                junit 'build/test-results/test/*.xml'

                echo "Generating Cucumber reports"
                bat 'gradlew.bat cucumberReports'

                archiveArtifacts artifacts: 'build/reports/cucumber/html/**', fingerprint: true
            }
        }

        // ===========================
        stage('Code Analysis') {
            steps {
                echo "Running SonarQube analysis"
                withSonarQubeEnv(SONARQUBE) {
                    bat 'gradlew.bat sonarqube'
                }
            }
        }

        // ===========================
        stage('Code Quality') {
            steps {
                echo "Waiting for Quality Gate"
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ===========================
        stage('Build') {
            steps {
                echo "Building Jar & documentation"
                bat 'gradlew.bat jar generateDocs'

                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                archiveArtifacts artifacts: 'build/docs/**', fingerprint: true
            }
        }

        // ===========================
        stage('Deploy') {
            steps {
                echo "Publishing to Maven repository"
                bat 'gradlew.bat publish'
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS ✅"
            echo "Notifications handled by Gradle (Slack + Mail)"
        }

        failure {
            echo "Pipeline FAILED ❌"
            echo "Notifications handled by Gradle (Slack + Mail)"
        }
    }
}

pipeline {
    agent any

    environment {
        SLACK_WEBHOOK = credentials('slack-webhook')
    }

    options {
        skipStagesAfterUnstable()
    }

    stages {

        // ===========================
        stage('Test') {
            steps {
                echo "Phase Test: Lancement des tests unitaires"
                bat 'gradlew.bat clean test'

                echo "Archivage des résultats des tests unitaires"
                junit 'build/test-results/test/*.xml'

                echo "Génération des rapports de tests Cucumber"
                script {
                    try {
                        bat 'gradlew.bat generateCucumberReports'
                        publishHTML([
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'build/reports/cucumber/html',
                            reportFiles: 'overview-features.html',
                            reportName: 'Cucumber Report'
                        ])
                    } catch (Exception e) {
                        echo "Avertissement: Impossible de générer les rapports Cucumber: ${e.message}"
                    }
                }
            }
        }

        // ===========================
        stage('Code Analysis') {
            steps {
                echo "Analyse du code avec SonarQube"
                withSonarQubeEnv('sonar') {
                    bat 'gradlew.bat sonar'
                }
            }
        }

        // ===========================
        stage('Code Quality') {
            steps {
                echo "Vérification des Quality Gates"
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ===========================
        stage('Build') {
            steps {
                echo "Génération du Jar et de la documentation"
                bat 'gradlew.bat jar javadoc'

                echo "Archivage du fichier Jar"
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true

                echo "Archivage de la documentation"
                archiveArtifacts artifacts: 'build/docs/javadoc/**', fingerprint: true, allowEmptyArchive: true
            }
        }

        // ===========================
        stage('Deploy') {
            steps {
                echo "Déploiement du Jar sur Maven repo"
                bat 'gradlew.bat publish'
            }
        }
    }

    post {

        always {
            echo "Nettoyage et archivage des artefacts"
        }

        success {
            echo "Pipeline terminé avec succès"

            script {
                // Email
                emailext(
                    to: "lh_boulacheb@esi.dz",
                    subject: "✅ Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline exécuté avec succès</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>URL:</strong> ${env.BUILD_URL}</p>
                    """,
                    mimeType: 'text/html'
                )

                // Slack
                bat """
                    curl -X POST -H "Content-type: application/json" ^
                    --data "{\\"text\\":\\"✅ Pipeline SUCCESS\\nProjet: ${env.JOB_NAME}\\nBuild: #${env.BUILD_NUMBER}\\nURL: ${env.BUILD_URL}\\"}" ^
                    %SLACK_WEBHOOK%
                """
            }
        }

        failure {
            echo "Pipeline échoué"

            script {
                // Email
                emailext(
                    to: "lh_boulacheb@esi.dz",
                    subject: "❌ Pipeline Failure: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline échoué</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>Statut:</strong> FAILURE</p>
                        <p><strong>Logs:</strong> ${env.BUILD_URL}console</p>
                    """,
                    mimeType: 'text/html'
                )

                // Slack
                bat """
                    curl -X POST -H "Content-type: application/json" ^
                    --data "{\\"text\\":\\"❌ Pipeline FAILURE\\nProjet: ${env.JOB_NAME}\\nBuild: #${env.BUILD_NUMBER}\\nLogs: ${env.BUILD_URL}console\\"}" ^
                    %SLACK_WEBHOOK%
                """
            }
        }

        unstable {
            echo "Pipeline instable"

            script {
                // Email
                emailext(
                    to: "lh_boulacheb@esi.dz",
                    subject: "⚠️ Pipeline Unstable: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline instable</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>URL:</strong> ${env.BUILD_URL}</p>
                    """,
                    mimeType: 'text/html'
                )

                // Slack
                bat """
                    curl -X POST -H "Content-type: application/json" ^
                    --data "{\\"text\\":\\"⚠️ Pipeline UNSTABLE\\nProjet: ${env.JOB_NAME}\\nBuild: #${env.BUILD_NUMBER}\\nURL: ${env.BUILD_URL}\\"}" ^
                    %SLACK_WEBHOOK%
                """
            }
        }
    }
}

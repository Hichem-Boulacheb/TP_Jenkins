pipeline {
    agent any
     triggers {
            githubPush()
      }
    options {
        skipStagesAfterUnstable()
    }

    stages {

        // ===========================
        stage('Test') {
            steps {
                echo "Phase Test:  Lancement des tests unitaires"
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
                        echo "Avertissement: Impossible de générer les rapports Cucumber:  ${e.message}"
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
                archiveArtifacts artifacts:  'build/docs/javadoc/**', fingerprint: true, allowEmptyArchive: true
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
            script {
                echo "Pipeline terminé avec succès"

                emailext(
                    to: "lh_boulacheb@esi.dz",
                    subject: "✅ Pipeline Success:  ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline exécuté avec succès</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    """,
                    mimeType: 'text/html'
                )

                withCredentials([string(credentialsId: 'SLACK_WEBHOOK', variable: 'SLACK_WEBHOOK_URL')]) {
                    bat """
                        curl -X POST -H "Content-type: application/json" --data "{\\"text\\":\\"Pipeline réussi\\n*Projet: * ${env.JOB_NAME}\\n*Build:* #${env.BUILD_NUMBER}\\n*URL:* ${env.BUILD_URL}\\",\\"username\\":\\"Jenkins\\",\\"icon_emoji\\":\\":white_check_mark:\\"}" %SLACK_WEBHOOK_URL%
                    """
                }
            }
        }

        failure {
            script {
                echo "Pipeline échoué"

                emailext(
                    to:  "lh_boulacheb@esi.dz",
                    subject: "❌ Pipeline Failure: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline échoué</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>Statut:</strong> FAILURE</p>
                        <p><strong>Logs:</strong> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                    """,
                    mimeType: 'text/html'
                )

                withCredentials([string(credentialsId:  'SLACK_WEBHOOK', variable: 'SLACK_WEBHOOK_URL')]) {
                    bat """
                        curl -X POST -H "Content-type: application/json" --data "{\\"text\\":\\"Pipeline échoué\\n*Projet:* ${env.JOB_NAME}\\n*Build: * #${env.BUILD_NUMBER}\\n*Logs:* ${env.BUILD_URL}console\\",\\"username\\":\\"Jenkins\\",\\"icon_emoji\\":\\":x:\\"}" %SLACK_WEBHOOK_URL%
                    """
                }
            }
        }

        unstable {
            script {
                echo "Pipeline instable"

                emailext(
                    to: "lh_boulacheb@esi.dz",
                    subject:  "⚠️ Pipeline Unstable: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Pipeline instable</h2>
                        <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    """,
                    mimeType: 'text/html'
                )

                withCredentials([string(credentialsId: 'SLACK_WEBHOOK', variable: 'SLACK_WEBHOOK_URL')]) {
                    bat """
                        curl -X POST -H "Content-type:  application/json" --data "{\\"text\\":\\"Pipeline instable\\n*Projet:* ${env.JOB_NAME}\\n*Build:* #${env.BUILD_NUMBER}\\n*URL:* ${env.BUILD_URL}\\",\\"username\\":\\"Jenkins\\",\\"icon_emoji\\":\\":warning:\\"}" %SLACK_WEBHOOK_URL%
                    """
                }
            }
        }
    }
}
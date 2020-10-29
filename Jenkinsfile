// This Jenkinsfile is used by Jenkins to run the Wikidata step of Reactome's release.
// It requires that the ConfirmReleaseConfigs step has been run successfully before it can be run.

import java.text.DateFormatSymbols
import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline {
	agent any
	
	environment {
	    EXPORTER_OUTPUT_FOLDER = getOutputFolderPath()
	}
	
	stages{
		stage('User Input: Get release news URL'){
		    steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def baseReleaseNewsURL = "https://reactome.org/about/news/"
					def userInputReleaseNewsURL = input(
					id: 'userInput', message: "Please paste the URL to Reactome's release announcement for v${releaseVersion} below.",
					parameters: [
						[$class: 'TextParameterDefinition', description: "News items can be found at found at ${baseReleaseNewsURL}", name: 'response']
					])

					if (userInputReleaseNewsURL.contains("${baseReleaseNewsURL}")) {
						echo("Valid URL submitted: ${userInputReleaseNewsURL} . Running Wikidata Exporter step now.")
						env.RELEASE_NEWS_URL = "${userInputReleaseNewsURL}"
					} else {
						error("Invalid URL. Please submit proper URL corresponding to Reactome's v${releaseVersion} release found at ${baseReleaseNewsURL}.")
					}
				}
		    }
		}
        stage('Setup: Clone Wikidata Bot repository') {
            steps{
                script{
                    utils.cloneOrUpdateLocalRepo("r-wikidata-bot")
                    dir("r-wikidata-bot") {
                        sh "git checkout origin/feature-revamp-bot"
                        sh "python3 -m pipenv install --ignore-pipfile"
                    }
                }
            }
        }
        // This stage builds the jar file using maven.
		stage('Setup: Build jar files'){
			steps{
				script{
					utils.buildJarFile()
				}
			}
		}
		stage('Main: Run Wikidata-Exporter'){
			steps{
				script{
					sh "mkdir -p ${env.EXPORTER_OUTPUT_FOLDER}"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/wikidata-exporter-jar-with-dependencies.jar --user ${user} --password ${pass} --outputdirectory ${env.EXPORTER_OUTPUT_FOLDER}"
					}
				}
			}
		}
		stage('Main: Run R-Wikidata-Bot') {
		    steps{
		        script{
		            withCredentials([file(credentialsId: 'Config', variable: 'ConfigFile')]){
		              def releaseDate = getReleaseDateFromConfigFile("${ConfigFile}")
		              def releaseYear = "${releaseDate}".split("-")[0]
		              def releaseMonth = getReleaseMonthName("${releaseDate}".split("-")[1].toInteger())
		              def releaseDay = "${releaseDate}".split("-")[2]
        		      dir("r-wikidata-bot") {
        		          sh "python3 -m pipenv run python bot.py -d ${env.EXPORTER_OUTPUT_FOLDER} -rnews ${env.RELEASE_NEWS_URL} -rday ${releaseDay} -rmonth ${releaseMonth} -ryear ${releaseYear} --fastrun" //--write
            		    }
		            }
		        }
		    }
		}
	}
}

def getOutputFolderPath() {
    return pwd() + "/data/"
}

def getReleaseDateFromConfigFile(configFile) {
    def releaseDate = sh (
            script: "grep dateOfRelease $configFile | cut -d = -f2",
            returnStdout: true
            ).trim()
    return releaseDate
}

def getReleaseMonthName(releaseMonthNumber) {
    releaseMonthNumber--
    return new DateFormatSymbols().getMonths()[releaseMonthNumber]
}

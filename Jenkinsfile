// This Jenkinsfile is used by Jenkins to run the WikidataExporter step of Reactome's release.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline {
	agent any
	// Sets absolute path of 'data' folder that will be output by WikidataExporter and subsequently used by r-wikidata-bot
	environment {
	    EXPORTER_OUTPUT_FOLDER = getOutputFolderPath()
	}
	
	stages{
		// Asks for user to submit URL for Reactome Release news item.
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

					// Checks that the submitted URL contains the string in $baseReleaseNewsURL
					if (userInputReleaseNewsURL.contains("${baseReleaseNewsURL}")) {
						echo("Valid URL submitted: ${userInputReleaseNewsURL} . Running Wikidata Exporter step now.")
						env.RELEASE_NEWS_URL = "${userInputReleaseNewsURL}"
					} else {
						error("Invalid URL. Please submit proper URL corresponding to Reactome's v${releaseVersion} release found at ${baseReleaseNewsURL}.")
					}
				}
		    	}
		}
		// Clones/Pulls from r-wikidata-bot repository
        	stage('Setup: Clone r-wikidata-bot repository and set up environment') {
            		steps{
                		script{
                    			utils.cloneOrUpdateLocalRepo("r-wikidata-bot")
                    			dir("r-wikidata-bot") {
////TODO:					Before merging this branch into develop, the below line needs to be removed!
                        			sh "git checkout master"
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
		// Runs the Wikidata-Exporter step that populates the env.EXPORTER_OUTPUT_FOLDER with 5 JSON files corresponding to
		// Reactome data structures. Typically, this should be the 'data' folder.
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
		// Before running r-wikidata-bot in write mode, a dry run is complete to ensure program running correctly.
		stage('Main: Dry run of r-wikidata-bot') {
			steps{
		    		script{
		     			withCredentials([file(credentialsId: 'Config', variable: 'configFile')]){
		              			runWikidataBot("$configFile", false)
						// Since this program is run twice before clean up (once for dry run, once for write run), 
						// but always produces a file called 'bot.log', the dry run file must be renamed.
		              			dir("r-wikidata-bot") {
		              				sh "mv -f --backup=numbered bot.log dry-run-bot.log"
		             	 		}
		           		}
		        	}
		    	}
		}
		// Sends email notification to mailing list regarding completion of r-wikidata-bot dry run.
		stage('Post: Email regarding completion of dry run'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def wd = "release.reactome.org:" + pwd() + "/r-wikidata-bot/"
					def emailSubject = "Wikidata exporter update for v${releaseVersion}"
					def emailBody = "Hello,\n\nThis is an automated message from Jenkins regarding an update for v${releaseVersion}. The dry run for r-wikidata-bot has completed. Please review the \'dry-run-bot.log\' file at ${wd} and confirm that it looks correct, before proceeding with the \'write\' phase. \n\nThanks!"
					utils.sendEmail("${emailSubject}", "${emailBody}")
				}
			}
		}
		// Program is stopped and waits for confirmation that the dry run was successful.
		stage('User Input: Confirm successful dry run of R-Wikidata-Bot') {
			steps {
		        	script {
		            		def userInput = input(
						id: 'userInput', message: "Please confirm dry run of r-wikidata-bot was successful.",
						parameters: [
							[$class: 'BooleanParameterDefinition', defaultValue: true, name: 'response']
						])
		       		}
		    	}
		}
		// Actually runs the r-wikidata-bot in write mode. This makes use of credentials implicitly accessible to the Jenkins user.
		stage('Main: Run R-Wikidata-Bot in write mode') {
			steps{
		        	script{
		            		withCredentials([file(credentialsId: 'Config', variable: 'ConfigFile')]){
		                		runWikidataBot("$configFile", true)
		            		}
		        	}
		    	}
		}
		// Archives all output from the step and stores it on S3, before deleting everything on the release server to preserve space.
		stage('Post: Archive Outputs') {
			steps {
		        	script {
					def releaseVersion = utils.getReleaseVersion()
					// This clean up method will take listed files and put them in corresponding files.
					// Since WikidataExporter already puts its output data files into a 'data' folder, nothing needs to be specified.
					def dataFiles = []
					def logFiles = ["WikidataExporter.log", "r-wikidata-bot/*bot.log*"]
					def foldersToDelete = ["r-wikidata-bot*"]
					utils.cleanUpAndArchiveBuildFiles("wikidata_exporter", dataFiles, logFiles, foldersToDelete)
		        	}
		    	}
		}
	}
}

// Helper methods specific to the Wikidata/r-wikidata-bot Jenkinsfile

// Used for specifying WikidataExporter 'outputdirectory'.
def getOutputFolderPath() {
	return pwd() + "/data/"
}

// Runs r-wikidata-bot code, with a boolean argument for running it in write mode or not.
def runWikidataBot(configFile, writeMode) {
	// Parses releaseDate variable from config file, which is in the format yyyy-mm-dd.
	def releaseDate = getReleaseDateFromConfigFile("${configFile}")
	def releaseYear = "${releaseDate}".split("-")[0]
	// The rmonth argument needs a text version of the month, so this method just converts the integer value to the name value.
	def releaseMonth = "${releaseDate}".split("-")[1]
	def releaseDay = "${releaseDate}".split("-")[2]
	
	def wikidataCmd = "python3 -m pipenv run python bot.py -d ${env.EXPORTER_OUTPUT_FOLDER} -rnews ${env.RELEASE_NEWS_URL} -rday ${releaseDay} -rmonth ${releaseMonth} -ryear ${releaseYear} --fastrun"
	if (writeMode) {
		wikidataCmd = wikidataCmd + " --write"
	}
	// Actual execution of the command.
	dir("r-wikidata-bot") {
		sh "${wikidataCmd}"
	}
}

// Parses the releaseDate variable from config file, which is in the format yyyy-mm-dd, using a piped grep/cut command.
def getReleaseDateFromConfigFile(configFile) {
	def releaseDate = sh (
		script: "grep dateOfRelease $configFile | cut -d = -f2",
		returnStdout: true
	).trim()
	return releaseDate
}

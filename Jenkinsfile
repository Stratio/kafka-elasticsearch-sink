@Library('libpipelines@master') _

hose {
    MAIL = 'governance'
    LANG = 'scala'
    SLACKTEAM = 'data-governance'
    MODULE = 'kafka-elasticsearch-sink'
    REPOSITORY = 'github.com/kafka-elasticsearch-sink'
    DEVTIMEOUT = 30
    RELEASETIMEOUT = 30
    MAXITRETRIES = 2

    ITSERVICES = []

    ITPARAMETERS = ""

    DEV = {
        config ->
            doCompile(config)
            doPackage(config)
            doDocker(config)
    }
}

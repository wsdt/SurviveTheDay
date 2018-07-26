


# SurviveTheDay  [![Maintenance](https://img.shields.io/badge/Maintained%3F-no-red.svg)](https://bitbucket.org/lbesson/ansi-colors) [![GitHub license](https://img.shields.io/github/license/wsdt/SurviveTheDay.svg)](https://github.com/wsdt/SurviveTheDay/blob/master/LICENSE) [![Generic badge](https://img.shields.io/badge/In-Java-RED.svg)](https://www.java.com/)
Android App - Countdown for bad things 

## Playstore
You can find this app on PlayStore. 
- FREEMIUM: https://play.google.com/store/apps/details?id=kevkevin.wsdt.tagueberstehen
- PREMIUM: https://play.google.com/store/apps/details?id=kevkevin.wsdt.tagueberstehen.paid

_All (senseful) contributions/pull-requests, which have been examined, might get published on both of these versions on Playstore. (if you want)._

## Issue handling
### How to add an issue
1. **Mark relating code with a relevant annotation (relating to your labels).** E.g. @Bug(params), @Enhance(params)
1. **Please assign your new issue to a relevant project (e.g. Alpha, Beta or Production).** Normally you want to assign new issues to the Alpha project, because you want to have the relating code in all version layers above (Alpha > Beta > Production). Only assign new issues to e.g. Beta if you know that the issue is only in the Beta-Version or layers above (e.g. Production) and isn't solved/isn't necessary to be solved in the Alpha Version. If you don't assign your issue to a project then it is dangling and won't show up in the projects overview. 
1. **Assign relevant labels to your issue.** If you want to report a bug, then add the bug label. For new features there is a creation label, so as for smaller changes we have introduced the enhancement label. You can also assign multiple labels to one issue. 
1. **Add a good title to your issue.** Please use a concise and precise title. 
  * *BAD*: "ServiceMgr"
  * *GOOD*: "Redesign/Improve ServiceMgr"
1. **Add a good description to your issue.** Your description doesn't need to be concise, but should be clear/understandable and provide enough information for other contributors to solve the issue. *Your issue description SHOULD/MUST look like the following: *

########## EXAMPLE ISSUE - START ##########
- {Short description in 1-3 sentences}
- {Describe the problem, new feature or what you want to change}
- {Describe a possible solution to that problem, feature or desired code}
- {Add a list of classes which might need to be changed in order to implement the new feature/solve the bug. Please append the class filetype (Human.class) so we can avoid confusions. If a class hasn't been created yet, then you can use a recommended name by adding the [N] cue to it. (e.g.: Human.class[N])}
- {Maybe also add videos/pictures} *(OPTIONAL)*

########## EXAMPLE ISSUE - END ##########

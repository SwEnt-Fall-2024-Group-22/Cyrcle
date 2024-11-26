# Milestone M2: Team Feedback

This milestone M2 provides an opportunity to give you, as a team, formal feedback on how you are performing in the project. By now, you should be building upon the foundations set in M1, achieving greater autonomy and collaboration within the team. This is meant to complement the informal, ungraded feedback from your coaches given during the weekly meetings or asynchronously on Discord, email, etc.

The feedback focuses on two major themes:
First, whether you have adopted good software engineering practices and are making progress toward delivering value to your users.
Is your design and implementation of high quality, easy to maintain, and well tested?
Second, we look at how well you are functioning as a team, how you organize yourselves, and how well you have refined your collaborative development.
An important component is also how much you have progressed, as a team, since the previous milestone.
You can find the evaluation criteria in the [M2 Deliverables](https://github.com/swent-epfl/public/blob/main/project/M2.md) document.
As mentioned in the past, the standards for M2 are elevated relative to M1, and this progression will continue into M3.

We looked at several aspects, grouped as follows:

 - Design
   - [Features](#design-features)
   - [Design Documentation](#design-documentation)
 - [Implementation and Delivery](#implementation-and-delivery)
 - Scrum
   - [Backlogs Maintenance](#scrum-backlogs-maintenance)
   - [Documentation and Ceremonies](#scrum-documentation-and-ceremonies)
   - [Continuous Delivery of Value](#scrum-continuous-delivery-of-value)

## Design: Features

We interacted with your app from a user perspective, assessing each implemented feature and flagging any issues encountered. Our evaluation focused mainly on essential features implemented during Sprints 3, 4, and 5; any additional features planned for future Sprints were not considered in this assessment unless they induced buggy behavior in the current APK.
We examined the completeness of each feature in the current version of the app, and how well it aligns with user needs and the overall project goals.



Your app starts to shape up really well and you are getting closer to having all the core features, congrats ! 
The features implemented are almost fully completed and without any noticeable major bug. However, if the user doesn't accept to reveal its location, the app loses at lot of its features -> list is not relevant anymore and doesn't display anyhing. A suggestion would be to add a textfield and use a reverse geocoding API to allow the user to specify where he wants to see information from.

For the following sprints, the buttons that are for now not leading anywhere could be a good starting point and you could start to build the community aspect of your app to have more fun-to-use product. Uploading and correctly displaying pictures seems to also be a relevant step for your project


For this part, you received 6.3 points out of a maximum of 8.0.

## Design: Documentation

We reviewed your Figma (including wireframes and mockups) and the evolution of your overall design architecture in the three Sprints.
We assessed how you leveraged Figma to reason about the UX, ensure a good UX, and facilitate fast UI development.
We evaluated whether your Figma and architecture diagram accurately reflect the current implementation of the app and how well they align with the app's functionality and structure.


**Figma**:
Your Figma is up to date with most of your features, the only one that wasn't there is the pinning of parkings, but we can understand that it is a difficult feature to add in a mockup. The Figma is of good quality and the UI looks good !

**Architecture diagram**:
The architecture diagram is up to date, well thought and precise, nice work! One thing that could be improved is that the diagram doesn't really account for the features you planned to add in future sprints -> Wallet/Coins, etc... 
It should be used in the same way than a Figma: sketch the concept first and then  implement them


For this part, you received 5.4 points out of a maximum of 6.0.

## Implementation and Delivery

We evaluated several aspects of your app's implementation, including code quality, testing, CI practices, and the functionality and quality of the APK.
We assessed whether your code is well modularized, readable, and maintainable.
We looked at the efficiency and effectiveness of your unit and end-to-end tests, and at the line coverage they achieve.


**Code quality**:
Overall, the quality of your code is good and you made great progress since the first milestone! The documentation is generally well written and you are using tools that are not trivial to get around (Hilt, MapBox (seems more complex than google map)), but you managed to use them correctly, nice work ! 


**Tests and CI**:
According to your screenshot, you have achieved 80% line coverage, which is good! You have a CI configured to run the tests, but as we already knew, there is still no system to report the coverage directly (sonar or equivalent). We would really like that you find an alternative for this for the last milestone.
Your provided screenshots in your PRs, but the issue is that you generally posted a screen of the "app layer" which wasn't fully reflective of what the coverage on *new code* was, please do more precise ones with the files that you have modified.

The tests are of a good quality, but in some cases, you could have gone deeper into edge cases and test the expected behaviour. Also, you could try to test more the components/atoms of you project to achieve an overall higher line coverage. 

**APK functionality**:
The APK provided a smooth experience, we did not encounter any crashes or severe bug. Here are some advices/Issues we found:
- Is there a particular reason for locking the rotation on the map ? It feels a bit counter intuitive since most of the app have this enabled and it helps you navigate the map with more ease.
- If we enable the advanced mode, the parkings are no longer clickable and the one in the starlink just disappears
- You've handled the "not yet developped" actions well with toasts, but as an advice, if a button is not applicable, just don't show it. For example, in the review screen, it would be better to disable (gray'ish color) the delete review button if you haven't posted any
- The pinning works well, we suggest to not remove it from the original list but that's totally up to you.
- The capacity filter has weird conditions since you can select multiple ones and it kind of contradicts itsef. I can choose <10 and 10 - 25 at the same time
- You have a good check on the area of the parking, but this can be tricked by making an extremly long parking, but a small width -> could add a check on the length of the border of the rectangle
Overall, good job, the app works well !



For this part, you received 14.4 points out of a maximum of 16.0.

## Scrum: Backlogs Maintenance

We looked at whether your Scrum board is up-to-date and well organized.
We evaluated your capability to organize Sprint 6 and whether you provided a clear overview of this planning on the Scrum board.
We assessed the quality of your user stories and epics: are they clearly defined, are they aligned with a user-centric view of the app, and do they suitably guide you in delivering the highest value possible.


**Sprint backlog**
It is generally well organized, everyone has assigned tasks and the scrum board is rather clean, but some tasks inside the SB are lacking basic informations such as the expected time and the priority. Please try to improve this for the next sprints

**Product Backlog**
Your product backlog contains some very relevant user stories for your app and is up to date with the state of your app, nice work ! However, there are two tasks that are placed inside this column (at the end) and this should not happen


For this part, you received 3 points out of a maximum of 4.0.

## Scrum: Documentation and Ceremonies

We assessed how you used the Scrum process to organize yourselves efficiently.
We looked at how well you documented your team Retrospective and Stand-Up during each Sprint.
We also evaluated your autonomy in using Scrum.


**SCRUM Documents**
Your scrum documents were well completed with some detailed explanation on what you are going through and on time, nice work!

**SCRUM Meetings**
The meetings are well-structured and most of the team members show real engagement during the meetings. You always ask questions or discuss about how to improve certain features or fix some problems which shows your seriousness and professionalism. You also don't hesitate to ask your questions and it is always good to ask for external feedback, congratulations !

**Autonomy**
You have showed strong autonomy starting from Sprint 3. We rarely need to intervene as you are very organized for the meetings. The Scrum Masters and Product Owners did a very good job.This is excellent work!


For this part, you received 4 points out of a maximum of 4.0.

## Scrum: Continuous Delivery of Value

We evaluated the Increment you delivered at the end of each Sprint, assessing your team’s ability to continuously add value to the app.
This included an assessment of whether the way you organized the Sprints was conducive to an optimal balance between effort invested and delivery of value.


Your team is generally consistent in the produced work, even during difficult weeks with midterm, good job !


For this part, you received 1.8 points out of a maximum of 2.0.

## Summary

Based on the above points, your intermediate grade for this milestone M2 is 5.36. If you are interested in how this fits into the bigger grading scheme, please see the [project README](https://github.com/swent-epfl/public/blob/main/project/README.md) and the [course README](https://github.com/swent-epfl/public/blob/main/README.md).

Your coaches will be happy to discuss the above feedback in more detail.

Good luck for the next Sprints!

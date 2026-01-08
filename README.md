# Sleep Analyzer App

This is a fork of [plees-tracker](https://github.com/vmiklos/plees-tracker) that attempts to modernize it and add further features.

Check out the [user guide](https://vmiklos.hu/plees-tracker/) for plees-tracker for the 
original list of features, installation, and usage information.

## Technical changes

- Converted code to use MVVM-pattern
- Completely removed the DataModel singleton and made many new files to handle what DataModel did before
- All ViewModels now have factorys and use dependency injection

## Added feature(s)

### Analysis of sleeping environments sound level
Measures the dB level from the microphone and determines if the audio level is suitable for sleeping
<img width="358" height="789" alt="image" src="https://github.com/user-attachments/assets/aa004a8f-dfc9-4f99-af41-5c5c66985b56" />


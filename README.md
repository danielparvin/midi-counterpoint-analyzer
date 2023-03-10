# MIDI Analyzer
## Description
MIDI Analyzer is a music theory analysis tool. It reads MIDI files, which represent the timing of the notes played by different instruments or "tracks" throughout a piece of music. This tool supplements the manual analysis of a musical composition by programmatically calculating counterpoint statistics.   
"Counterpoint" is the branch of music theory focused on the interplay of voices within a piece. The three basic types of contrapuntal motion are "similar," "oblique," and "contrary." If two voices both move in the same direction, they are said to have similar motion. If one voice remains the same while the other moves up or down, the motion between the two voices is oblique. And if the two voices move in opposite directions, they exhibit contrary motion in that place.   
By uploading a ZIP file containing multiple MIDI files, users can analyze many pieces at once and evaluate trends. For example, users could upload two different sets of Bach fugues to evaluate whether Bach tended to use more contrary motion in major-key fugues than in minor-key fugues. Similarly, users could upload various sets of MIDI files to evaluate trends in a composer's use of counterpoint by instrumentation, key signature, tempo, or date of composition, or to compare the use of counterpoint among distinct composers.

## Technologies
- Java Spring Boot
- Thymeleaf
- javax.sound.midi
- JFreeChart

## Project Status
In development
### To-do List
- Finish styling the Analysis page (Wednesday-Saturday).
  - Adjust legend to drop below pie chart on small screens (Friday).
  - Adjust "Upload another MIDI file!" section to appear next to "Files analyzed" section on large screens (Friday).
  - Add legend to (or next to) histogram image (Friday).
  - Style the list of uploaded files and add a reset button (Saturday). (Replace bullets with musical note icons. Provide distinct background and text color to the section.)
- Generate downloadable CSV files of the pie chart and histogram results (Saturday).
  - Refactor PNG-generation code (Saturday).
- Provide a demo MIDI file for users who want to quickly try the app without having to find or download MIDI files themselves (Monday) (https://www.mutopiaproject.org/cgibin/piece-info.cgi?id=1780).
- Make most of the Analysis page conditional upon the presence of an analysis (Monday).
- Analyze Bach's Two-part Inventions.
  - Evaluate contrapuntal motion trends in fast vs. slow inventions (Tuesday).
- Ask colleagues for feedback (Wednesday).
- Incorporate feedback (Monday, March 20).
- Record a demo video, describing and demonstrating the app, the Bach Inventions case study, and the limitations of the algorithm (Tuesday, March 21).

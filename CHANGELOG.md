1/3/2012 0.8.0

* Jon's updates:
* Moved source code to github (with Matt Snover's permission)
* Changed to LGPL license
* Moved all code under the "ter" package
* Removed most static variables and methods to allow TER to be used as a libarary in multi-threaded applications (for MultEval)
* Added generic parameters to collections, etc.
* Cleaned up most compiler warnings


8/19/08	Version 0.7.25

* Tokenization changed so that 's is correctly tokenized when using -N flag. No other changes from version 0.7.2.


3/26/08	 Version 0.7.2

* Several bug fixes. Scores might vary slightly from version 0.7.0.


3/27/07	 Version 07

* First full Java release
* Several bug fixes.


4/28/06	 Basic Java Classes

* Bug fix, and an increase in the beam width (to 20) for minimum edit distance. 
* Contains some basic Java classes which can be used to calculate TER. It is much faster than the perl script but the UI is not very developed. It is useful if you wish to examine the algorithm to more fully understand how it works. This code is presented here for research and educational purposes. It was designed to be integrated into NIST's HTER annotation tool. A test wrapper for the code is given, but it is not as functional as the tercom script. 
* The full version contains this code, plus bug fixes and additional functionality.


4/10/06	 Version 6b

* Added document level scoring (.sum_doc output) 
* Added support for n-best list scoring (.sum_nbest output) 
* Added option -N to normalize hyp and ref as in MT eval scoring 
* Increased precision of floating point output


11/10/05  Version 5

* Changed program name from calc_ter to tercom 
* Changed default options (-f 2 is now default) 
* Added support for sgm files (-i sgm) 
* Added option to specify beam width

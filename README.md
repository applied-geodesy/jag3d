Java·Applied·Geodesy·3D
=======================
Java·Applied·Geodesy·3D (JAG3D) is one of the most popular open source least-squares software package for geodetic sciences. The Java-based application is distributed under the terms of the GNU General Public License (version 3). JAG3D is designed to combine hybrid terrestrial observations like leveling, directions, distances or vertical angles in a uniform and rigorous mathematical model. Moreover, GNSS baselines that are derived by Global Navigation Satellite System (GNSS) techniques are supported. Some reference projects are the [deformation analysis](https://software.applied-geodesy.org/en/deformation_analysis) at the Onsala Space Observatory, the rigorous adjustment of laser tracker measurements on the observation level at the [electron accelerator](https://software.applied-geodesy.org/en/electron_accelerator) S‑DALINAC, or the analysis of local tie networks at [fundamental stations](https://software.applied-geodesy.org/en/fundamental_station) to improve global products like the International Terrestrial Reference Frame (ITRF). More information about the least-squares software package JAG3D can be found at [software.applied-geodesy.org](https://software.applied-geodesy.org/)</a>.

![Java Applied Geodesy 3D (JAG3D)](/.images/jag3d.png?raw=true "Java·Applied·Geodesy·3D (JAG3D)")

International Course on Engineering Surveying 2026
---------------------------------------------
The method of least-squares developed by Gauss is one of the most important analytical tools in geodesy and metrology for ensuring accurate and reliable data analysis. In engineering geodesy, it plays a crucial role in analysing both planned and existing geodetic networks and in verifying their congruence. Beside the coordinates of network, the method provides the dispersion of the estimated parameters and essential quantities for quality evaluation.
At the _21th International Course on Engineering Surveying_, the principles of geodetic network adjustment are recapitulated within the specific tutorial named _network analysis_. The conference will be held at TU Graz from February 10 to 13, 2026 - save the date. The network analysis tutorial offers a practical introduction to the fundamentals of adjustment calculations. Through guided case studies, participants will explore how adjustment methods work and gain a deeper understanding of the key parameters involved. Based on this knowledge, the congruence of networks is evaluated within the framework of deformation analysis. Moreover, the tutorial demonstrates how the adjustment results can be integrated into existing workflows and further processed. For the practical part, the open source least-squares software package Java·Applied·Geodesy·3D is used. 
Join fellow users, get hands-on experience with JAG3D, connect directly with the developer, and expand your professional network during the [Course on Engineering Surveying](https://ingenieurvermessungskurs.com/). See you soon in Graz!

[![International Course on Engineering Surveying](https://raw.githubusercontent.com/applied-geodesy/jag3d/master/.images/ingenieurvermessung26.png?raw=true "International Course on Engineering Surveying 2026")](https://ingenieurvermessungskurs.com/)

<!---
[comment] International Course on Engineering Surveying
---------------------------------------------
Diagnosis adjustment of planned networks as well as the analysis of existing geodetic networks by means of least-squares adjustment is a principal task in geodetic and metrological sciences such as surveying engineering. The network adjustment yields not only the estimated coordinates and the related fully populated dispersion but also parameters to evaluate the reliability of the network. At the *20th International Course on Engineering Surveying*, the principles of geodetic network adjustment were recapitulated within the specific tutorial named [network analysis](https://software.applied-geodesy.org/en/quality_assurance). The conference was held at ETH Zurich from April 11 to 14, 2023. The open source least-squares software package Java·Applied·Geodesy·3D was used to analyse local terrestrial networks, to interpret the adjustment results, and to evaluate network reliability. The principles of Baarda's DIA approach for detection, identification and adaptation of misspecified models were generalised and applied to evaluate network deformations. In complex analysis procedures, the network adjustment is only a cog in the wheel. For that reason, interfaces for data exchange and processing of adjustment results in external applications was addressed.
-->

Evaluation of Compatibility among Network Adjustment Software
-------------------------------------------------------------
Together with colleagues from Conservatoire National des Arts et Métiers (CNAM), European Organization for Nuclear Research (CERN), and School of Management and Engineering Vaud (HEIG-VD), we evaluated the compatibility among network adjustment software packages. A detailed description of the procedure and the results was presented at the 5th Joint International Symposium on Deformation Monitoring [JISDM 2022](https://jisdm2022.webs.upv.es). 

We compared the results of several geodetic networks using software packages developed by the authors' institutions, namely Compensation de Mesures Topographiques (CoMeT), Logiciel Général de Compensation (LGC), Trinet+ as well as JAG3D. Moreover, we included further commercial software packages such as Columbus, Geolab, Move3 and Star*Net. The networks differ mainly in their extent, i.e. the side length and the height. Whereas the smallest network is about 30 m, the largest network under consideration is about 40 km. The height component varies in a range from 30 m to 2.5 km. The raw data and the obtained results can be found on the official [CNAM website](https://comet.esgt.cnam.fr/comparisons).

It is a very important initiative to ensure the quality of adjustment packages. For that reason, the results obtained by JAG3D are available. The largest network extent is about 40 km, and the curvature of the Earth can no longer be neglected. The results obtained by JAG3D differ by about 20 µm for the horizontal and the vertical component w.r.t. the designed values. [JAG3D offers comparable results](https://doi.org/10.5281/zenodo.7468733) to reputable software packages.


Verified according to ISO 10360-6
-------------------------------------
The calibration of measuring equipment is essential in geodesy and metrology. In length metrology, for instance, the calibration enables the traceability of measured values to the SI base unit of the meter. High demands are placed on the standards used to provide reference values in order to ensure conformity with national and international standards and norms such as DIN or ISO. In Germany, the [Physikalisch-Technische Bundesanstalt](https://www.ptb.de/) (PTB) as the national metrology institute and supreme technical authority for metrology is responsible for this task.

The calibration of measuring instruments has a certain self-image at least in geodesy. However, the traceability of adjustment algorithms in software packages used is often not taken into account. According to the ISO 10360-6, which deals with the _estimation of errors in computing Gaussian associated features_, the PTB offers a service to certify metrological adjustment algorithms called _Traceability for Computational-Intensive Metrology_ (TraCIM).

JAG3D's module JUniForm, short for Java·Unified·Form·Fitting, has successfully passed the verification test of the algorithms for computing Gaussian associated features. Several synthetic generated data sets with modeled random and systematic deviations were used to test the implemented algorithms for basic features such as straight line, plane, circle, cylinder, sphere and cone. The resulting maximum permissible errors (MPE) of the estimated parameters were always less than the specified threshold values 0.1 μm and 0.1 μrad.

Proper handling of measured values is complex and requires not only the consideration of calibrated measuring instruments but also the use of reliable adjustment algorithms for a holistic evaluation of the full measuring process. More details can be found in the full [TraCIM report](https://software.applied-geodesy.org/doc/PTB_TraCIM_Test_Report_JAG3D.pdf).


Adaptable and Flexible
----------------------
The JAG3D report uses the powerful [FreeMarker](https://freemarker.apache.org) Template Language to generate text output via customized templates. Users have the option of creating specific templates for their individual workflow – without programming experience in Java. In addition to column-based flat files, complex HTML or XML files are also supported. In software packages for computer-aided design (CAD), the Drawing Interchange Format (DXF) is often used for the exchange between different applications. As these examples are pure text files, specific FreeMarker templates can be defined to generate the content of the files. The JAG3D [download package](https://github.com/applied-geodesy/jag3d/releases/latest) contains an template for creating the standard HTML report. Sample templates for creating simple ASCII or DXF files can be found in the [template collection](https://github.com/loesler/jag3d_templates).


System Requirements
-------------------
JAG3D is written in the platform-independent programming language Java and, therefore, the software is runnable at each platform and operation system that provides a Java Runtime Environment (JRE) and the [JavaFX](https://openjfx.io). JavaFX is included to the provided bundles. Moreover, the windows download package of JAG3D contains [OpenJDK](https://openjdk.java.net). For that reason, neither Java nor the platform dependent FX extension must be provided by the windows operating system. To run JAG3D on other platforms such as Linux or MacOS, the platform dependent JRE must be installed at the operating system. The JRE can be found for several platforms at Oracles [download page](https://java.oracle.com) or at the [OpenJDK](https://openjdk.java.net)-project pages.


Support and Installation
------------------------
JAG3D is a portable least-squares software package - no need to install or uninstall. Just [download the latest version](https://github.com/applied-geodesy/jag3d/releases/latest), unpack and run. The JAG3D manual is organized by a [Wiki](https://software.applied-geodesy.org/wiki/). Training videos are compiled in a [playlist](https://www.youtube.com/playlist?list=PLyOqiH7SWWC94Zmi5TVT7ClDqQWNrjbJ1). Moreover, a [support forum](https://software.applied-geodesy.org/forum/) is available for technical support. 


References
----------
- Heißelmann, D., Franke, M.: Java·Applied·Geodesy·3D - Evaluation software based on least-squares method for coordinate measuring machines. Traceability for Computational-Intensive Metrology, No. Y4QG8HDUOS76HT5S, Physikalisch-Technische Bundesanstalt, Braunschweig, 2023. [10.5281/zenodo.8217114](https://doi.org/10.5281/zenodo.8217114)

- Lösler, M.: Evaluation of Compatibility among Network Adjustment Software - Results from the JAG3D software package. Data-Set, 2023. [10.5281/zenodo.7468733](https://doi.org/10.5281/zenodo.7468733)

- Durand, S., Lösler, M., Jones, M., Cattin, P.-H., Guillaume, S., Morel, L.: Quantification of the dependence of the results on several network adjustment applications. In: García-Asenjo, L., Lerma, J. L. (eds.): 5th Joint International Symposium on Deformation Monitoring (JISDM), Editorial Universitat Politècnica de València, Spain, pp. 69-77, 2022. [10.4995/JISDM2022.2022.13671](https://doi.org/10.4995/JISDM2022.2022.13671)

- Herrmann, C., Lösler, M., Bähr, H.: Comparison of SpatialAnalyzer and Different Adjustment Programs. In: Kutterer, H., Seitz, F., Schmidt, M. (eds.): Proceedings of the 1st International Workshop on the Quality of Geodetic Observation and Monitoring Systems (QuGOMS'11) International Association of Geodesy Symposia, Vol. 140, Springer, pp. 79-84, 2015. DOI: [10.1007/978-3-319-10828-5_12](https://doi.org/10.1007/978-3-319-10828-5_12)

- Lösler, M., Bähr, H.: Vergleich der Ergebnisse verschiedener Netzausgleichungsprogramme. In: Zippelt, K. (ed.): Vernetzt und ausgeglichen - Festschrift zur Verabschiedung von Prof. Dr.-Ing. habil. Dr.-Ing. E.h. Günter Schmitt, KIT Scientific Publishing, pp. 205-214, 2010. DOI: [10.5445/KSP/1000020074](https://doi.org/10.5445/KSP/1000020074)

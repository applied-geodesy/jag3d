Java·Applied·Geodesy·3D
=======================
Java·Applied·Geodesy·3D (JAG3D) is one of the most popular open source least-squares software package for geodetic sciences. The Java-based application is distributed under the terms of the GNU General Public License (version 3). JAG3D is designed to combine hybrid terrestrial observations like leveling, directions, distances or vertical angles in a uniform and rigorous mathematical model. Moreover, GNSS baselines that are derived by Global Navigation Satellite System (GNSS) techniques are supported. Some reference projects are the [deformation analysis](https://software.applied-geodesy.org/en/deformation_analysis) at the Onsala Space Observatory, or the adjustment of laser tracker measurements at the [electron accelerator](https://software.applied-geodesy.org/en/electron_accelerator) S-DALINAC. More information about the least-squares software package JAG3D can be found at [software.applied-geodesy.org](https://software.applied-geodesy.org/)</a>. 


Screenshot
----------
![Java Applied Geodesy 3D (JAG3D)](/.images/jag3d.png?raw=true "Java·Applied·Geodesy·3D (JAG3D)")


System requirements
-------------------
JAG3D is written in the platform-independent programming language Java and, therefore, the software is runnable at each platform and operation system that provides a Java Runtime Environment (JRE) and the [JavaFX](https://openjfx.io). JavaFX is included to the provided bundles. Moreover, the windows download package of JAG3D contains [OpenJDK](https://openjdk.java.net). For that reason, neither Java nor the platform dependent FX extension must be provided by the windows operating system. To run JAG3D on other platforms such as Linux or MacOS, the platform dependent JRE must be installed at the operating system. The JRE can be found for several platforms at Oracles [download page](https://java.oracle.com) or at the [OpenJDK](https://openjdk.java.net)-project pages.


Support and Installation
------------------------
JAG3D is a portable least-squares software package - no need to install or uninstall. Just [download the latest version](https://github.com/applied-geodesy/jag3d/releases/latest), unpack and run. The JAG3D manual is organized by a [Wiki](https://software.applied-geodesy.org/wiki/). Training videos are compiled in a [playlist](https://www.youtube.com/playlist?list=PLyOqiH7SWWC94Zmi5TVT7ClDqQWNrjbJ1). Moreover, a [support forum](https://software.applied-geodesy.org/forum/) is available for technical support. 


Evaluation of Compatibility among Network Adjustment Software
-------------------------------------------------------------
Together with colleagues from Conservatoire National des Arts et Métiers (CNAM), European Organization for Nuclear Research (CERN), and School of Management and Engineering Vaud (HEIG-VD), we evaluated the compatibility among network adjustment software packages. A detailed description of the procedure and the results was presented at the 5th Joint International Symposium on Deformation Monitoring [JISDM 2022](https://jisdm2022.webs.upv.es). 

We compared the results of several geodetic networks using software packages developed by the authors' institutions, namely Compensation de Mesures Topographiques (CoMeT), Logiciel Général de Compensation (LGC), Trinet+ as well as JAG3D. Morover, we included further commercial software packages such as Columbus, Geolab, Move3 and Star*Net. The networks differ mainly in their extent, i.e. the side length and the height. Whereas the smallest network is about 30 m, the largest network under consideration is about 40 km. The height component varies in a range from 30 m to 2.5 km. The raw data and the obtained results can be found on the official [CNAM website](https://comet.esgt.cnam.fr/comparisons).

It is a very important initiative to ensure the quality of adjustment packages. For that reason, the results obtained by JAG3D are available. The largest network extent is about 40 km, and the curvature of the Earth can no longer be neglected. The results obtained by JAG3D differ by about 20 µm for the horizontal and the vertical component w.r.t. the designed values. JAG3D offers comparable results to reputable software packages.

References
----------

- Durand, S., Lösler, M., Jones, M., Cattin, P.-H., Guillaume, S., Morel, L.: Quantification of the dependence of the results on several network adjustment applications. 5th Joint International Symposium on Deformation Monitoring (JISDM), 20.-22. Juni 2022, Polytechnische Universität Valencia (UPV), Valencia, Spanien, 2022.

- Herrmann, C., Lösler, M., Bähr, H.: Comparison of SpatialAnalyzer and Different Adjustment Programs. In: Kutterer, H., Seitz, F., Schmidt, M. (Hrsg.): Proceedings of the 1st International Workshop on the Quality of Geodetic Observation and Monitoring Systems (QuGOMS'11) International Association of Geodesy Symposia Volume 140, Springer, S. 79-84, 2015. DOI: [10.1007/978-3-319-10828-5_12](https://doi.org/10.1007/978-3-319-10828-5_12)

- Lösler, M., Bähr, H.: Vergleich der Ergebnisse verschiedener Netzausgleichungsprogramme. Geodätisches Institut Karlsruhe (Hrsg.): Vernetzt und ausgeglichen - Festschrift zur Verabschiedung von Prof. Dr.-Ing. Dr. E.h. Günter Schmitt, KIT Scientific Publishing, S. 205-214, 2010. DOI: [10.5445/KSP/1000020074](https://doi.org/10.5445/KSP/1000020074)

/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: hfq
 * Apr 3, 2013
 */
package pt.up.fe.dceg.neptus.plugins.vtk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import pt.up.fe.dceg.neptus.NeptusLog;
import pt.up.fe.dceg.neptus.i18n.I18n;
import pt.up.fe.dceg.neptus.mra.MRAPanel;
import pt.up.fe.dceg.neptus.mra.api.BathymetryInfo;
import pt.up.fe.dceg.neptus.mra.importers.IMraLogGroup;
import pt.up.fe.dceg.neptus.mra.visualizations.MRAVisualization;
import pt.up.fe.dceg.neptus.plugins.PluginDescription;
import pt.up.fe.dceg.neptus.plugins.mra3d.Marker3d;
import pt.up.fe.dceg.neptus.plugins.vtk.filters.DownsamplePointCloud;
import pt.up.fe.dceg.neptus.plugins.vtk.geo.EarthSource;
import pt.up.fe.dceg.neptus.plugins.vtk.pointcloud.MultibeamToPointCloud;
import pt.up.fe.dceg.neptus.plugins.vtk.pointcloud.PointCloud;
import pt.up.fe.dceg.neptus.plugins.vtk.pointtypes.PointXYZ;
import pt.up.fe.dceg.neptus.plugins.vtk.visualization.Axes;
import pt.up.fe.dceg.neptus.plugins.vtk.visualization.Window;
import vtk.vtkActorCollection;
import vtk.vtkCanvas;
import vtk.vtkEarthSource;
import vtk.vtkGeoSource;
import vtk.vtkLODActor;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;

/**
 * @author hfq
 *
 */
@PluginDescription(author = "hfq", name = "Vtk")
public class Vtk extends JPanel implements MRAVisualization {
    private static final long serialVersionUID = 1L;
    
    private static Boolean vtkEnabled = true;
    
        // there are 2 types of rendering objects on VTK - vtkPanel and vtkCanvas. vtkCanvas seems to have a better behaviour and performance.
    //public vtkPanel vtkPanel;
    public vtkCanvas vtkCanvas;
    
    public Window winCanvas;
    
    private JToggleButton zExaggerationToggle;
    private JToggleButton rawPointsToggle;
    private JToggleButton downsampledPointsToggle;
    private JToggleButton meshToogle;
    private JButton resetViewportButton;
    private JButton helpButton;    
    private JPanel toolBar;

    private static final String FILE_83P_EXT = ".83P";
    
    public LinkedHashMap<String, PointCloud<PointXYZ>> linkedHashMapCloud = new LinkedHashMap<>();       
    public PointCloud<PointXYZ> pointCloud;
 
    private Vector<Marker3d> markers = new Vector<>();
    
    public IMraLogGroup mraVtkLogGroup;
    public File file;
    
    private Boolean componentEnabled = false;

    private DownsamplePointCloud performDownsample;
    private Boolean isDownsampleDone = false;
    
    static {  
        try {
            System.loadLibrary("jawt");
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load jawt lib!");
        } 
            // for simple visualizations
        try {
            vtkNativeLibrary.COMMON.LoadLibrary();
            if(!vtkNativeLibrary.COMMON.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkCommon, skipping...");
        }
        try {
            vtkNativeLibrary.FILTERING.LoadLibrary();
            if(!vtkNativeLibrary.FILTERING.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkFiltering, skipping...");
        }
        try {
            vtkNativeLibrary.IO.LoadLibrary();
            if(!vtkNativeLibrary.IO.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkImaging, skipping...");
            System.out.println("cannot load vtkImaging, skipping...");
        }
        try {
            vtkNativeLibrary.GRAPHICS.LoadLibrary();
            if(!vtkNativeLibrary.GRAPHICS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkGrahics, skipping...");
            System.out.println("cannot load vtkGrahics, skipping...");
        }
        try {
            vtkNativeLibrary.RENDERING.LoadLibrary();
            if(!vtkNativeLibrary.RENDERING.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkRendering, skipping...");
            System.out.println("cannot load vtkRendering, skipping...");
        }
                
        // Other
        try {
            vtkNativeLibrary.INFOVIS.LoadLibrary();
            if(!vtkNativeLibrary.INFOVIS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkInfoVis, skipping...");
        }
        try {
            vtkNativeLibrary.VIEWS.LoadLibrary();
            if(!vtkNativeLibrary.VIEWS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkViews, skipping...");
        }
        try {
            vtkNativeLibrary.WIDGETS.LoadLibrary();
            if(!vtkNativeLibrary.WIDGETS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkWidgets, skipping...");
        }
        try {
            vtkNativeLibrary.GEOVIS.LoadLibrary();
            if(!vtkNativeLibrary.GEOVIS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkGeoVis, skipping...");
        }
        try {
            vtkNativeLibrary.CHARTS.LoadLibrary();
            if(!vtkNativeLibrary.CHARTS.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkCharts, skipping...");
        }
        try {
            vtkNativeLibrary.VOLUME_RENDERING.LoadLibrary();
            if(!vtkNativeLibrary.VOLUME_RENDERING.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkVolumeRendering, skipping...");
        }
        // FIXME nao load vtkHybrid
        try {
            vtkNativeLibrary.HYBRID.LoadLibrary();
            if(!vtkNativeLibrary.HYBRID.IsLoaded())
                vtkEnabled = false;
        }
        catch (Throwable e) {
            NeptusLog.pub().info("<###> cannot load vtkHybrid, skipping...");
        }
    }
    
    /**
     * @param panel
     */
    public Vtk(MRAPanel panel) {
        super(new BorderLayout()); // change to MigLayout

        vtkCanvas = new vtkCanvas();

        /*              
            vtkPanel.GetRenderer().ResetCamera();
            //vtkPanel.GetRenderer().ResetCameraClippingRange();
            //vtkPanel.GetRenderer().LightFollowCameraOn();
            //vtkPanel.GetRenderer().VisibleActorCount();
            //vtkPanel.GetRenderer().ViewToDisplay();
         */
                       
        //AxesActor axesActor = new AxesActor(vtkCanvas.GetRenderer());
        //axesActor.setAxesVisibility(true);   
        
        pointCloud = new PointCloud<>();
        pointCloud.setCloudName("multibeam");
        linkedHashMapCloud.put(pointCloud.getCloudName(), pointCloud);
        
        winCanvas = new Window(vtkCanvas, linkedHashMapCloud);
        
        vtkCanvas.GetRenderer().ResetCamera();
        
            // add vtkCanvas to Layout
        add(vtkCanvas, BorderLayout.CENTER);
        
        toolBar = new JPanel();
        toolBar = createToolbar();
        add(toolBar, BorderLayout.SOUTH);
    }

    @Override
    public String getName() {

        return "Multibeam 3D";
    }

    @Override
    public Component getComponent(IMraLogGroup source, double timestep) {    
        if (!componentEnabled)
        {
            vtkCanvas.LightFollowCameraOn();
            vtkCanvas.BeginBoxInteraction();
            vtkCanvas.setEnabled(true);
            
            //System.out.println("Entrou no component Enabled");
            componentEnabled = true;    

            MultibeamToPointCloud multibeamToPointCloud = new MultibeamToPointCloud(getLog(), pointCloud);
            //BathymetryInfo batInfo = new BathymetryInfo();
            //batInfo = multibeamToPointCloud.batInfo;
            pointCloud.createLODActorFromPoints();
            
            Axes ax = new Axes(30.0, 0.0f, 0.0f, 0.0f, 0);
            vtkCanvas.GetRenderer().AddActor(ax.getAxesActor());
            //ax.getAxesActor().SetVisibility(true);
            
            //vtkLODActor randomActor = new vtkLODActor();
            //PointCloud<PointXYZ> randomCloud = new PointCloud<>();
            //randomActor = randomCloud.getRandomPointCloud(1000);
            //System.out.println("randomCloud number of points: " + randomCloud.getNumberOfPoints());
            
            
            //performDownsample = new DownsamplePointCloud(randomCloud, 0.1);
            //performDownsample = new DownsamplePointCloud(pointCloud, 0.1);

            //PointCloud<PointXYZ> downsampledCloud = performDownsample.getOutputDownsampledCloud();
            //linkedHashMapCloud.put(downsampledCloud.getCloudName(), downsampledCloud); 
            
            vtkCanvas.GetRenderer().AddActor(pointCloud.getCloudLODActor());
            //vtkCanvas.GetRenderer().AddActor(downsampledCloud.getCloudLODActor());
            
            //vtkLODActor tempActor = performDownsample.getOutputDownsampledCloud().getCloudLODActor();
            //System.out.println("Number of Cloud Points: " + tempActor.GetNumberOfCloudPoints());
            //vtkCanvas.GetRenderer().AddActor(tempActor);
                       
            //NeptusLog.pub().info("<###> VtkCanvas Graphics configuration: " + vtkCanvas.getGraphicsConfiguration().toString());
            
            vtkCanvas.GetRenderer().ResetCamera();
            //vtkCanvas.GetRenderer().ResetCameraClippingRange();
        }
        return this;
    }

    @Override
    public boolean canBeApplied(IMraLogGroup source) {
        boolean beApplied = false;        
        //System.out.println("CanBeApplied: " + source.name());
        //System.out.println("vtkEnabled can be applied: " + vtkEnabled);
        
        if (vtkEnabled == true) {   // if it could load vtk libraries
                // Checks existance of a *.83P file
            file = source.getFile("Data.lsf").getParentFile();
            File[] files = file.listFiles();
            try {
                if (file.isDirectory()) {
                    for (File temp : file.listFiles()) {
                        if ((temp.toString()).endsWith(FILE_83P_EXT)) {
                            setLog(source);
                            beApplied = true;
                        }  
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return beApplied;
    }


    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Double getDefaultTimeStep() {
        return null;
    }

    @Override
    public boolean supportsVariableTimeSteps() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.VISUALIZATION;
    }

    @Override
    public void onHide() {
    }

    @Override
    public void onShow() {
    }

    @Override
    public void onCleanup() {
//        try {
//            vtkPanel.disable();
//            //vtkPanel.Delete();
//        }
//        catch (Throwable e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
    
    /**
     * @return the mraVtkLogGroup
     */
    private IMraLogGroup getLog() {
        return mraVtkLogGroup;
    }

    /**
     * @param mraVtkLogGroup the mraVtkLogGroup to set
     */
    private void setLog(IMraLogGroup log) {
        this.mraVtkLogGroup = log;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBackground(Color.DARK_GRAY);
        //toolbar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        //toolbar.setAutoscrolls(true);
        //Rectangle rect = new Rectangle();
        //rect.height = 50;
        //rect.height = 50;
        //toolbar.setBounds(rect);
        
        rawPointsToggle = new JToggleButton(I18n.text("Raw"));
        rawPointsToggle.setBounds(toolbar.getX(), toolbar.getY(), toolbar.getWidth(), 10);
        downsampledPointsToggle = new JToggleButton(I18n.text("Downsampled"));
        downsampledPointsToggle.setBounds(rawPointsToggle.getBounds());
        
        zExaggerationToggle = new JToggleButton(I18n.text("Exaggerate Z"));
        
        meshToogle = new JToggleButton(I18n.text("Show Mesh"));
        
        resetViewportButton = new JButton(I18n.text("Reset Viewport"));
        helpButton = new JButton(I18n.text("Help"));
        
        rawPointsToggle.setSelected(true);
        downsampledPointsToggle.setSelected(false);
        meshToogle.setSelected(false);
        zExaggerationToggle.setSelected(false);
        
        //resetViewportToggle.setSelected(false);
        
        helpButton.setSize(10, 10);
        helpButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                String msgHelp;
                msgHelp = "\tHelp for the 3D visualization interaction:\n\n";
                msgHelp = msgHelp + "Key    -   description\n";
                msgHelp = msgHelp + "p, P   -   switch to a point-based representation\n";
                msgHelp = msgHelp + "w, W   -   switch to a wireframe-based representation, when available\n";
                msgHelp = msgHelp + "s, S   -   switch to a surface-based representation, when available\n";
                msgHelp = msgHelp + "j, J   -   take a .PNG snapshot of the current window view\n";
                msgHelp = msgHelp + "g, G   -   display scale grid (on/off)\n";
                msgHelp = msgHelp + "u, U   -   display lookup table (on/off)\n";
                msgHelp = msgHelp + "r, R   -   reset camera (to viewpoint = {0, 0, 0} -> center {x, y, z}\n";
                msgHelp = msgHelp + "i, I   -   information about rendered cloud\n";
                msgHelp = msgHelp + "f, F   -   press right mouse and then f, to fly to point picked\n"; 
                msgHelp = msgHelp + "3      -   3D visualization (put the 3D glasses on)\n";
                msgHelp = msgHelp + "7      -   color gradient in relation with X coords (north)\n";
                msgHelp = msgHelp + "8      -   color gradient in relation with Y coords (west)\n";
                msgHelp = msgHelp + "9      -   color gradient in relation with Z coords (depth)\n"; 
 
                JOptionPane.showMessageDialog(null, msgHelp);
            }
        });
        
        rawPointsToggle.addActionListener(new ActionListener() {       
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rawPointsToggle.isSelected()) {
                    
                }
                else {
                    
                }
            }
        });
        
        downsampledPointsToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (downsampledPointsToggle.isSelected()) {
//                    try {
//                        System.out.println("Before collection");
//                        vtkActorCollection actorCollection = new vtkActorCollection();
//                        actorCollection =  vtkCanvas.GetRenderer().GetActors();
//                        actorCollection.InitTraversal();                  
//                        for (int i = 0; i < actorCollection.GetNumberOfItems(); ++i) {
//                            vtkCanvas.GetRenderer().RemoveActor(actorCollection.GetNextActor());
//                        }
//                        System.out.println("After collection");
//                        
//                        vtkCanvas.GetRenderer().Render();
//                        
//                        PointCloud<PointXYZ> downsampledCloud = new PointCloud<>();
//                        
//                        if (!isDownsampleDone) {    
//                            PointCloud<PointXYZ> multibeamCloud = new PointCloud<>();
//                            multibeamCloud = linkedHashMapCloud.get("multibeam");
//                            
//                            performDownsample = new DownsamplePointCloud(multibeamCloud, 0.5);
//
//                            downsampledCloud = performDownsample.getOutputDownsampledCloud();
//                            linkedHashMapCloud.put(downsampledCloud.getCloudName(), downsampledCloud); 
//                        }
//                        vtkCanvas.GetRenderer().AddActor(downsampledCloud.getCloudLODActor());
//                    }
//                    catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
                }
                else {
                    
                }
            }
        });
        
        meshToogle.addActionListener(new ActionListener() {        
            @Override
            public void actionPerformed(ActionEvent e) {
                if (meshToogle.isSelected()) {
                    try {
                        
                        vtkActorCollection actorCollection = new vtkActorCollection();
                        actorCollection =  vtkCanvas.GetRenderer().GetActors();
                        actorCollection.InitTraversal(); 
                        
                        NeptusLog.pub().info("<###> Number of actors on render: " + actorCollection.GetNumberOfItems());
                        
                        
                        vtkCanvas.GetRenderer().RemoveAllViewProps();
                        vtkCanvas.GetRenderWindow().Render();

                        //for (int i = 0; i < actorCollection.GetNumberOfItems(); ++i) {
                        //    vtkActor actor = actorCollection.GetNextActor();
                            //System.out.println("actor num: " + i + "actor.string: " + actor.toString());


                            //vtkCanvas.GetRenderer().RemoveActor(actorCollection.GetNextActor());
                        //}
                        //System.out.println("After collection");
                      

                        EarthSource earth = new EarthSource();
                        vtkCanvas.GetRenderer().AddActor(earth.getEarthActor());
                        
                        //vtkCanvas.GetRenderer().Render();
                        vtkCanvas.GetRenderWindow().Render();
                        vtkCanvas.getRenderWindowInteractor().Render();
                        vtkCanvas.GetRenderer().ResetCamera();                     
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                else {
                    
                }
            }
        });
        
        zExaggerationToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zExaggerationToggle.isSelected()) {
                    
                }
                else {
                    
                }
            }
        });
        
        resetViewportButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                vtkCanvas.GetRenderer().ResetCamera();
                vtkCanvas.getRenderWindowInteractor().Render();
            }
        });

            // toogles
        toolbar.add(rawPointsToggle);
        toolbar.add(downsampledPointsToggle);
        toolbar.add(meshToogle);
        toolbar.add(zExaggerationToggle);
            // buttons
        toolbar.add(resetViewportButton);
        toolbar.add(helpButton);
        
        return toolbar;
    }
}

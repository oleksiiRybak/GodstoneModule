package com.oleksii;


import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.livestreamrecord.manager.ILiveStreamRecordManager;
import com.wowza.wms.livestreamrecord.manager.ILiveStreamRecordManagerActionNotify;
import com.wowza.wms.livestreamrecord.manager.IStreamRecorder;
import com.wowza.wms.livestreamrecord.manager.IStreamRecorderConstants;
import com.wowza.wms.livestreamrecord.manager.IStreamRecorderFileVersionDelegate;
import com.wowza.wms.livestreamrecord.manager.StreamRecorder;
import com.wowza.wms.livestreamrecord.manager.StreamRecorderParameters;
import com.wowza.wms.livestreamrecord.model.ILiveStreamRecord;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaStreamMap;
import com.wowza.wms.vhost.IVHost;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

public class GodstoneModule extends ModuleBase implements IModuleOnApp {
	
    private Map<String, ILiveStreamRecord> recorders = new HashMap<String, ILiveStreamRecord>();
    private IApplicationInstance appInstance = null;
    private IVHost vhost = null;
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_FLV = 1;
    public static final int FORMAT_MP4 = 2;
    String firstRecordFileName = null;    

    public void onAppStart(IApplicationInstance appInstance) {
    	GodstoneModule.getLogger().info("ModuleAutoRecordAdvancedExample onAppStart[" + appInstance.getContextStr() + "]: ");
        this.appInstance = appInstance;
        this.vhost = appInstance.getVHost();
        this.vhost.getLiveStreamRecordManager().addListener((ILiveStreamRecordManagerActionNotify)new MyStreamRecorderListener());
    }

    public void onAppStop(IApplicationInstance appInstance) {
    }

    class MyFileVersionDelegate implements IStreamRecorderFileVersionDelegate {
        
    	 public String getFilename(IStreamRecorder recorder) {
             String name;
             try {
                 File file = new File(recorder.getBaseFilePath());
                 String oldBasePath = file.getParent();
                 String oldName = file.getName();
                 String oldExt = "";
                 int oldExtIndex = oldName.lastIndexOf(".");
                 if (oldExtIndex >= 0) {
                     oldExt = oldName.substring(oldExtIndex);
                     oldName = oldName.substring(0, oldExtIndex);
                 }
                 name = oldBasePath+"/"+oldName+"_"+DateTime.now().millisOfDay().getAsText()+oldExt;
                 file = new File(name);
 				if (file.exists()) {
 					file.delete();
 				}
                 if ((file = new File(name = String.valueOf(oldBasePath) + "/" + oldName + oldExt)).exists()) {
                 	name = oldBasePath+"/"+oldName+"_"+DateTime.now().millisOfDay().getAsText()+oldExt;                    
                 }
             }
             catch (Exception e) {
                 WMSLoggerFactory.getLogger(MyFileVersionDelegate.class).error("LiveStreamRecordFileVersionDelegate.getFilename: " + e.toString());
                 name = "junk.tmp";
             }
             return name;
         }
    }

    class MyStreamRecorderListener implements ILiveStreamRecordManagerActionNotify {
        private IVHost vhost;
        private StreamRecorderParameters recordParams;
        //protected String chOutputPath = "H:\\Wowza Archive\\"; //backup server path
        //protected String chOutputPath = "D:\\Wowza Archive\\content\\";
        //protected String chOutputPath = "C:\\Private\\Wowza Media Systems\\Wowza Streaming Engine 4.4.1\\content\\";
        //recordParams.outputPath = "C:\\Private\\Wowza Media Systems\\Wowza Streaming Engine 4.4.1\\content";
        protected String chOutputPath = "/usr/local/WowzaStreamingEngine-4.6.0/content/";
        private final String mp4Ext = ".mp4";
        XmlBuilder exb = new EnglishXmlBuilder();
        XmlBuilder xb = new XmlBuilder();
        
        MyStreamRecorderListener() {
            this.vhost = null;
        }

        public void onCreateRecord(IStreamRecorder recorder) {
        	GodstoneModule.getLogger().info("MyStreamRecorderListener.onCreateRecorder[" + GodstoneModule.this.appInstance.getContextStr() + "]: new Recording created:" + recorder.getStreamName());
        }

        public void onSplitRecord(IStreamRecorder recorder) {
        	GodstoneModule.getLogger().info("MyStreamRecorderListener.onSplitRecorder[" + GodstoneModule.this.appInstance.getContextStr() +
            		"]: Segment recording:" + recorder.getStreamName());
        }

        public void onStartRecord(IStreamRecorder recorder) {
          boolean versionFile = false;
          boolean startOnKeyFrame = true;
          boolean recordData = true;
          Iterator iterForStreams = null;
          IClient client = null;
          List publishStreams = new ArrayList();
          
          System.out.println("MyStreamRecorderListener.onStartRecorder[" + GodstoneModule.this.appInstance.getContextStr() + "]: new Recording started: " + recorder.getStreamName() + " " + recorder.getFilePath());
          
          this.vhost = GodstoneModule.this.appInstance.getVHost();
          client = recorder.getStream().getClient();
          MediaStreamMap streams = client.getAppInstance().getStreams();
          StreamRecorderParameters params = recorder.getRecorderParams();
          params.fileVersionDelegate = new GodstoneModule.MyFileVersionDelegate();          
          
          publishStreams.addAll(streams.getStreams());
          
          cleanStreamsFromRepetableOnes(publishStreams);
          
          System.out.println("MyStreamRecorderListener.publishStreams size: " + publishStreams.size());
          if (publishStreams != null & publishStreams.size() > 0) {
            iterForStreams = publishStreams.iterator();
          }
          while (iterForStreams.hasNext()) {
            IMediaStream stream = (IMediaStream)iterForStreams.next();
            System.out.println("MyStreamRecorderListener.streamName: " + stream.getName());
            if (this.vhost.getLiveStreamRecordManager().getRecorder(GodstoneModule.this.appInstance, stream.getName()) == null) {
             //if(!ifStreamIsAlreadyBeingRecorder(stream.getName())) {
            	
              switch (stream.getName()) {
              case "English3": 
            	  StreamRecorderParameters englishParameters = createStreamRecorderParameters(recorder);
                  if ((GodstoneModule.this.firstRecordFileName != null) && (GodstoneModule.this.firstRecordFileName.length() > 0)) {                	  
                	  englishParameters.outputFile = giveFile("HD");  
                	  //englishParameters.outputFile = (GodstoneModule.this.firstRecordFileName + "HD.mp4");                    
                  } 
                  runRecording(GodstoneModule.this.appInstance, stream.getName(), englishParameters);
                  
                  String xmlSrcEng = exb.createSmil(GodstoneModule.this.firstRecordFileName);
            	  try {
					exb.stringToDom(xmlSrcEng, chOutputPath, GodstoneModule.this.firstRecordFileName + ".smil");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				  }
                  GodstoneModule.this.firstRecordFileName = null;                  
                  break;
              case "French2": 
            	  StreamRecorderParameters frenchParameters = createStreamRecorderParameters(recorder);
                  if ((GodstoneModule.this.firstRecordFileName != null) && (GodstoneModule.this.firstRecordFileName.length() > 0)) {
                	frenchParameters.outputFile = giveFile("F");
                    //frenchParameters.outputFile = (GodstoneModule.this.firstRecordFileName + "F.mp4");
                  }
                  runRecording(GodstoneModule.this.appInstance, stream.getName(), frenchParameters);
                  
                  String xmlSrcFr = xb.createSmil(GodstoneModule.this.firstRecordFileName + "F");
            	  try {
					xb.stringToDom(xmlSrcFr, chOutputPath, GodstoneModule.this.firstRecordFileName + "F.smil");
				  } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				 }                  
                  GodstoneModule.this.firstRecordFileName = null;
                  break;
              case "Romanian2": 
            	  StreamRecorderParameters romanianParameters = createStreamRecorderParameters(recorder);
                  if ((GodstoneModule.this.firstRecordFileName != null) && (GodstoneModule.this.firstRecordFileName.length() > 0)) {                	  
                	  romanianParameters.outputFile = giveFile("R");
                	  //romanianParameters.outputFile = (GodstoneModule.this.firstRecordFileName + "R" + mp4Ext);
                  }
                  runRecording(GodstoneModule.this.appInstance, stream.getName(), romanianParameters);
                  
                  String xmlSrcRo = xb.createSmil(GodstoneModule.this.firstRecordFileName + "R");
            	  try {
					xb.stringToDom(xmlSrcRo, chOutputPath, GodstoneModule.this.firstRecordFileName + "R.smil");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                  GodstoneModule.this.firstRecordFileName = null; 	
                  break;
              }
            }
          }
        }

		private boolean ifStreamIsAlreadyBeingRecorder(String streamName) {
			Map<String, IStreamRecorder> recordingMap = vhost.getLiveStreamRecordManager().getRecordersMap(appInstance);
			Set<String> recorders = recordingMap.keySet();

			for(String key : recorders){
				IStreamRecorder recorder = recordingMap.get(key);
				int recordState = recorder.getRecorderState();
				String strName = recorder.getStreamName();
				if(strName == streamName) {
					return true;
				}
				switch(recordState){
				case IStreamRecorderConstants.RECORDER_STATE_ERROR:
				//do something
				break;
				case IStreamRecorderConstants.RECORDER_STATE_PENDING:
				//do something	
				break;		
				case IStreamRecorderConstants.RECORDER_STATE_WAITING:
				//do something
				break;
				case IStreamRecorderConstants.RECORDER_STATE_RECORDING:
				//do something
				return true;
			}
		  }
			return false;
		}
        
        private String giveFile(String langAttr) {
        	return getFilename(chOutputPath, GodstoneModule.this.firstRecordFileName + langAttr + mp4Ext);
        }
        
        public String getFilename(String filePath, String fileName) {
            String name;
            String oldExt = "";
            String oldName = "";
            
            try {
                File file = new File(filePath + fileName);
				if (file.exists()) {
					int oldExtIndex = fileName.lastIndexOf(".");
		            if (oldExtIndex >= 0) {
		                oldExt = fileName.substring(oldExtIndex);
		                oldName = fileName.substring(0, oldExtIndex);
		            }
					name = oldName+"_"+DateTime.now().millisOfDay().getAsText()+ mp4Ext;
				} else {
					name = fileName;
				}
            }
            catch (Exception e) {                
                name = "junk.tmp";
            }
            return name;
        }

        public StreamRecorderParameters createStreamRecorderParameters(IStreamRecorder recorder) {
            StreamRecorderParameters recordParams = new StreamRecorderParameters(GodstoneModule.this.appInstance);
            recordParams.fileFormat = "mp4";
            recordParams.startOnKeyFrame = true;
            recordParams.recordData = true;
            recordParams.segmentationType = "none";
            recordParams.versioningOption = IStreamRecorderConstants.VERSION_FILE;
            recordParams.fileVersionDelegate = new GodstoneModule.MyFileVersionDelegate();
            //recordParams.fileTemplate = new GodstoneModule.MyFileVersionDelegate().getFilename(recorder);
            //recordParams.outputPath = "D:\\Wowza Archive\\content\\";
            recordParams.outputPath = chOutputPath;
            
            File file = new File(recorder.getBaseFilePath());
            GodstoneModule.this.firstRecordFileName = file.getName();
            String oldExt = "";
            int oldExtIndex = GodstoneModule.this.firstRecordFileName.lastIndexOf(".");
            if (oldExtIndex >= 0) {
                oldExt = GodstoneModule.this.firstRecordFileName.substring(oldExtIndex);
                GodstoneModule.this.firstRecordFileName = GodstoneModule.this.firstRecordFileName.substring(0, oldExtIndex);
            }
            return recordParams;
        }

        private void cleanStreamsFromRepetableOnes(List<IMediaStream> streams) {

            if (streams != null && streams.size() > 0) {
                int i = 0;
                while (i < streams.size()) {
                    IMediaStream stream = streams.get(i);
                    if ("English1".equals(stream.getName())) {
                        streams.remove(i);
                        --i;
                    }
                    ++i;
                }
            }
        }

        private String parseOutputPath(String filePath) {
            return null;
        }

        public void runRecording(IApplicationInstance appInstance, String streamName, StreamRecorderParameters recordParams) {
            this.vhost.getLiveStreamRecordManager().startRecording(appInstance, streamName, recordParams);
        }

        public void onStopRecord(IStreamRecorder recorder) {
        	GodstoneModule.getLogger().info("MyStreamRecorderListener.onStopRecorder[" + GodstoneModule.this.appInstance.getContextStr() + "]: Recording stopped:" + recorder.getStreamName() + " "
        + recorder.getCurrentFile());
        }

        public void onSwitchRecord(IStreamRecorder recorder, IMediaStream newStream) {
        	GodstoneModule.getLogger().info("GodstoneModule.onSwitchRecorder[" + GodstoneModule.this.appInstance.getContextStr() + "]: switch to new stream, old Stream:" + recorder.getStreamName()
            + " new Stream:" + newStream.getName());
        }

        public IStreamRecorder recordFactory(String streamName, StreamRecorderParameters recordParams) {
            return null;
        }
    }

}
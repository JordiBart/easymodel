package cat.udl.easymodel.vcomponent.selectmodel.window;
//package cat.udl.easymodel.vcomponent.selectmodel.window;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//
//import com.vaadin.server.ErrorHandler;
//import com.vaadin.server.Page;
//import com.vaadin.shared.ui.window.WindowMode;
//import com.vaadin.ui.Alignment;
//import com.vaadin.ui.Notification;
//import com.vaadin.ui.Notification.Type;
//import com.vaadin.ui.Panel;
//import com.vaadin.ui.ProgressBar;
//import com.vaadin.ui.UI;
//import com.vaadin.ui.Upload;
//import com.vaadin.ui.Upload.FailedEvent;
//import com.vaadin.ui.Upload.FailedListener;
//import com.vaadin.ui.Upload.ProgressListener;
//import com.vaadin.ui.Upload.Receiver;
//import com.vaadin.ui.Upload.SucceededEvent;
//import com.vaadin.ui.Upload.SucceededListener;
//
//import cat.udl.easymodel.logic.model.Model;
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.sbml.SBMLTools;
//import cat.udl.easymodel.utils.p;
//
//import com.vaadin.ui.VerticalLayout;
//import com.vaadin.ui.Window;
//
//public class ImportSBMLVL extends VerticalLayout {
//	private static final long serialVersionUID = 1L;
//	private SessionData sessionData;
//	private ByteArrayOutputStream baos=null;
//	private Upload upload;
//	private ProgressBar progressBar;
//	private SelectModelRepositoryWindow smrw;
//
//	public ImportSBMLVL(SelectModelRepositoryWindow smrw) {
//		super();
//		this.sessionData = (SessionData) UI.getCurrent().getData();
//		this.smrw = smrw;
//		
//		upload = getUpload();
////		progressBar = getProgressBar();
//
//		this.setSpacing(true);
//		this.setMargin(false);
//		this.setDefaultComponentAlignment(Alignment.TOP_CENTER);
//		this.addComponents(upload);
//	}
//
//	private ProgressBar getProgressBar() {
//		ProgressBar bar = new ProgressBar(0.0f);
//		bar.setCaption("Upload progress");
//		return bar;
//	}
//
//	private Upload getUpload() {
//		Upload upload = new Upload("Import SBML from file (drag supported)", getReceiver());
//		upload.setButtonCaption("Import SBML");
//		upload.addSucceededListener(getSucceededListener());
//		upload.addFailedListener(getFailedListener());
//		upload.addProgressListener(getProgressListener());
//		upload.setErrorHandler(new ErrorHandler() {
//			
//			@Override
//			public void error(com.vaadin.server.ErrorEvent event) {
//				
//			}
//		});
//		return upload;
//	}
//
//	private ProgressListener getProgressListener() {
//		return new ProgressListener() {
//			@Override
//			public void updateProgress(long readBytes, long contentLength) {
//				float newVal = readBytes / contentLength;
//				progressBar.setValue(newVal);
//			}
//		};
//	}
//
//	private Receiver getReceiver() {
//		return new Receiver() {
//
//			@Override
//			public OutputStream receiveUpload(String filename, String mimeType) {
//				if (!"text/xml".equals(mimeType))
//					return null;
//				try {
//					baos = new ByteArrayOutputStream();
//				} catch (Exception e) {
//					return null;
//				}
//				return baos;
//			}
//		};
//	}
//
//	private SucceededListener getSucceededListener() {
//		return new SucceededListener() {
//
//			public void uploadSucceeded(SucceededEvent event) {
//				try {
//					Model m = SBMLTools.importSBML(new ByteArrayInputStream(baos.toByteArray()));
//					sessionData.setSelectedModel(m);
//					smrw.setData(true);
//					smrw.close();
//				} catch (Exception e) {
//					Notification.show("SBML file contains errors: "+e.getMessage(), Type.WARNING_MESSAGE);
//					e.printStackTrace();
//				}
//			}
//		};
//	}
//
//	private FailedListener getFailedListener() {
//		return new FailedListener() {
//
//			@Override
//			public void uploadFailed(FailedEvent event) {
//				new Notification("Upload Fail (is file an SBML/XML?)", Notification.Type.WARNING_MESSAGE)
//						.show(Page.getCurrent());
//			}
//		};
//	}
//}

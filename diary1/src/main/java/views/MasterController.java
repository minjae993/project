package views;

import javafx.scene.layout.Pane;

public abstract class MasterController {
	private Pane root; //자신의 가장 윗쪽의 팬을 저장

	public Pane getRoot() {
		return root;
	}

	public void setRoot(Pane root) {
		this.root = root;
	}
	
	public abstract void init();
}

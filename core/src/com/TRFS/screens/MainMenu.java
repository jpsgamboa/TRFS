package com.TRFS.screens;

import com.TRFS.simulator.AssetsMan;
import com.TRFS.simulator.MiscUtils;
import com.TRFS.ui.general.UIButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * @author jgamboa
 *
 */
public class MainMenu implements Screen {
	
	private Stage stage = new Stage(new ScreenViewport());
	private Image logo = new Image(AssetsMan.manager.get(AssetsMan.TRFSLogo, Texture.class));
	private Table table;
	private UIButton 	buttonStart = new UIButton("START", "StartMenu", true, stage), 
						buttonSettings = new UIButton("SETTINGS", "Settings", true, stage), 
						buttonAbout = new UIButton("ABOUT", "AboutScreen", true, stage), 
						buttonExit = new UIButton("EXIT", true);
		
	@Override
	public void render(float delta) {
		MiscUtils.clearScreen();
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		MiscUtils.checkResize(width, height, stage);
		table.invalidateHierarchy();table.setSize(width, height);//Prevent objects from being resized
	}

	@Override
	public void show() {
		MiscUtils.fadeIn(stage);
		Gdx.input.setInputProcessor(stage); //Allows the stage to look for input
		
		//Create table and set bounds
		table = new Table();
		table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
							
		//Placing buttons in the table and setting their size
		int buttonW = 200, buttonH = 40, spaceBottom = 10;
		table.add(logo).size(388,116).spaceBottom(10*spaceBottom).row();
		table.add(buttonStart).size(buttonW, buttonH).spaceBottom(spaceBottom).row();
		table.add(buttonSettings).size(buttonW, buttonH).spaceBottom(spaceBottom).row();
		table.add(buttonAbout).size(buttonW, buttonH).spaceBottom(spaceBottom).row();
		table.add(buttonExit).size(buttonW, buttonH);
				
		stage.addActor(table);
		
		// TODO add ISEL logo
		// TODO add author name
	}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {
		stage.dispose();

	}
}

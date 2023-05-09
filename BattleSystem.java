import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class BattleSystem extends JFrame {
	List<Color> colors = Arrays.asList(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA);
	List<Integer> visibleIndices = Arrays.asList(0, 1, 2, 3, 4);
	
	JPanel[] panelArrayForAI = new JPanel[5];
	JPanel panel1, panel2, panel3, panel3Top, panel1Top, panel2Middle, playerActionPanel, aiActionPanel, actionPanel;
	JButton[] buttonArrayForPlayer = new JButton[5];
	JButton randomNumberButton, randomNumberButtonAI, randomNumberGeneratorButton;
	JLabel playerDamageLabel, aiDamageLabel, roundNumberLabel, playerWinsLabel, aiWinsLabel;
	JProgressBar progressBar;
	Timer timerDelay;
	Color grayColor = new Color(50, 50, 50);
	Color lightGrayColor = new Color(238, 238, 238);
	Timer fightTimer;
	Timer timerDmg;
	Timer timerRound;
	AtomicInteger finalPlayerDmg = new AtomicInteger(0);
	AtomicInteger finalAIDmg = new AtomicInteger(0);

	int[] randomNumbersOnButtonsPlayer, randomNumbersOnPanelsAI;
	int clickedButtonIndex, randIndexVisiblePanelAI;
	int finalDamagePlayer, finalDamageAI;
	int turnsCounter, clicksCounter, roundCounter, playerWinsCounter = 2, aiWinsCounter = 2;
	int stateManager;
	boolean playerTurn = true;
	
	public static final int PANEL_WIDTH = 390;
	public static final int PANEL_HEIGHT = 160;
	
	public BattleSystem() {
		initializeComponents();
		setLayouts();
	}

	void initializeComponents() {
		setSize(1200, 700);
		setResizable(false);
		panel1 = new JPanel();
		panel2 = new JPanel();
		panel3 = new JPanel();
		panel3Top = new JPanel();
		panel1.setBackground(Color.BLACK);
		panel2.setBackground(Color.WHITE);
		panel3.setBackground(Color.BLACK);
		initializePanel1();
		initializePanel2();
		initializePanel3();
		initializeVariablesForRandomNumber(buttonArrayForPlayer.length, panelArrayForAI.length);
		addRandomNumbers(buttonArrayForPlayer, panelArrayForAI);
	}

	void setLayouts() {
		getContentPane().setLayout(new GridLayout(1, 3));
		getContentPane().add(panel1);
		getContentPane().add(panel2);
		getContentPane().add(panel3);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	void initializePanel1() {
		panel1.setLayout(new BorderLayout());
		panel1Top = new JPanel();
		JPanel p1Bottom = new JPanel(new BorderLayout());
		setComponentSize(panel1Top, 400, 500);
		setComponentSize(p1Bottom, 400, 200);
		panel1Top.setBackground(Color.BLACK);
		p1Bottom.setBackground(Color.GRAY);
		JPanel cP = new JPanel(new BorderLayout());
		cP.setBackground(Color.GRAY);
		playerWinsLabel = new JLabel("");
		playerWinsLabel.setFont(new Font(playerWinsLabel.getFont().getName(), Font.PLAIN, 48));
		cP.add(playerWinsLabel, BorderLayout.EAST);
		p1Bottom.add(cP, BorderLayout.SOUTH);
		panel1.add(panel1Top, BorderLayout.NORTH);
		panel1.add(p1Bottom, BorderLayout.SOUTH);
		initializeButtons(panel1Top, buttonArrayForPlayer, true);
		for (int i = 0; i < buttonArrayForPlayer.length; i++) {
			buttonArrayForPlayer[i].setActionCommand(String.valueOf(i));
		}
	}

	void initializePanel2() {
		panel2.setLayout(new BorderLayout());
		JPanel p2Top = new JPanel(new BorderLayout());
		panel2Middle = new JPanel();
		JPanel p2Bottom = new JPanel();
		setComponentSize(p2Top, 400, 100);
		setComponentSize(panel2Middle, 400, 500);
		setComponentSize(p2Bottom, 400, 100);
		panel2Middle.setBackground(grayColor);
		createBorderBox(panel2Middle, lightGrayColor, 3);
		panel2.add(p2Top, BorderLayout.NORTH);
		panel2.add(panel2Middle, BorderLayout.CENTER);
		panel2.add(p2Bottom, BorderLayout.SOUTH);
		JPanel cP = new JPanel(new BorderLayout());
		playerDamageLabel = new JLabel(" ", SwingConstants.LEFT);
		playerDamageLabel.setFont(new Font(playerDamageLabel.getFont().getName(), Font.PLAIN, 24));
		cP.add(playerDamageLabel, BorderLayout.WEST);
		aiDamageLabel = new JLabel(" ", SwingConstants.RIGHT);
		aiDamageLabel.setFont(new Font(aiDamageLabel.getFont().getName(), Font.PLAIN, 24));
		cP.add(aiDamageLabel, BorderLayout.EAST);
		roundNumberLabel = new JLabel("" + (++roundCounter), SwingConstants.CENTER);
		roundNumberLabel.setFont(new Font(roundNumberLabel.getFont().getName(), Font.PLAIN, 24));
		cP.add(roundNumberLabel, BorderLayout.CENTER);
		p2Top.add(cP, BorderLayout.SOUTH);
		progressBar = new JProgressBar(0, 5);
		progressBar.setValue(5);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(false);
		progressBar.setFont(new Font("Arial", Font.PLAIN, 30));
		progressBar.setString("");
		p2Top.add(progressBar, BorderLayout.CENTER);
		timerDelay = new Timer(1000, e -> {
			int cV = progressBar.getValue();
			if (cV > 0) {
				cV--;
				progressBar.setValue(cV);
				progressBar.setString(Integer.toString(cV));
			}
			if (cV == 0 && stateManager == 0) {
				stateManager = 1;
				toggleTimerStatus(true);
				activateRandomVisibleButton(buttonArrayForPlayer);
				showRandomNumberButton();
				clearLabels();
				clicksCounter = 3;
			} else if (cV == 0 && stateManager == 1) {
				stateManager = 0;
				toggleTimerStatus(false);
				randomNumberButton.setText(Integer.toString(getRandomNumber()));
				randomNumberButtonAI.setText(Integer.toString(getRandomNumber()));
				hideButtons();
				updateLabels();
			}
		});
		toggleTimerStatus(true);
		randomNumberGeneratorButton = new JButton("RND");
		randomNumberGeneratorButton.setVisible(false);
		formatButton(randomNumberGeneratorButton);
		randomNumberButton = new JButton("");
		randomNumberButtonAI = new JButton("");
		formatButton(randomNumberButton);
		randomNumberGeneratorButton.addActionListener(e -> {
			randomNumberButton.setVisible(true);
			if (clicksCounter > 0) {
				randomNumberButton.setText(Integer.toString(getRandomNumber()));
				randomNumberButtonAI.setText(Integer.toString(getRandomNumber()));
				clicksCounter--;
			}
			if (clicksCounter == 0) {
				hideAndClearButtons();
			}
		});
		randomNumberButton.addActionListener(e -> {
			if (clicksCounter > 0) {
				hideAndClearButtons();
				fourRoundsBattle();
			}
		});
		p2Bottom.add(randomNumberGeneratorButton, BorderLayout.CENTER);
		p2Bottom.add(randomNumberButton, BorderLayout.CENTER);
	}

	void initializePanel3() {
		panel3.setLayout(new BorderLayout());
		JPanel p3Bottom = new JPanel(new BorderLayout());
		setComponentSize(panel3Top, 400, 500);
		setComponentSize(p3Bottom, 400, 200);
		panel3Top.setBackground(Color.BLACK);
		p3Bottom.setBackground(Color.GRAY);
		JPanel cP = new JPanel(new BorderLayout());
		cP.setBackground(Color.GRAY);
		aiWinsLabel = new JLabel("");
		aiWinsLabel.setFont(new Font(aiWinsLabel.getFont().getName(), Font.PLAIN, 48));
		cP.add(aiWinsLabel, BorderLayout.WEST);
		p3Bottom.add(cP, BorderLayout.SOUTH);
		panel3.add(panel3Top, BorderLayout.NORTH);
		panel3.add(p3Bottom, BorderLayout.SOUTH);
		initializeButtons(panel3Top, panelArrayForAI, false);
	}

	void initializeButtons(JPanel parentPanel, JComponent[] components, boolean isButton) {
		for (int i = 0; i < 5; i++) {
			if (isButton) {
				components[i] = new JButton();
				((JButton) components[i]).addActionListener(e -> buttonListener(e, buttonArrayForPlayer));
			} else {
				components[i] = new JPanel();
			}
			components[i].setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
			parentPanel.add(components[i]);
		}
		shuffleArrays(colors, visibleIndices);
		setButtonVisibility(components, colors, visibleIndices);
	}

	void initializeVariablesForRandomNumber(int bSize, int pAISize) {
		randomNumbersOnButtonsPlayer = new int[bSize];
		randomNumbersOnPanelsAI = new int[pAISize];
	}

	void addRandomNumbers(JButton[] b, JPanel[] pAI) {
		for (int i = 0; i < b.length; i++) {
			randomNumbersOnButtonsPlayer[i] = getRandomNumber();
			JLabel label = new JLabel(String.valueOf(randomNumbersOnButtonsPlayer[i]));
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setVerticalAlignment(SwingConstants.BOTTOM);
			label.setSize(b[i].getWidth(), b[i].getHeight());
			b[i].setLayout(new BorderLayout());
			b[i].add(label, BorderLayout.EAST);
		}
		for (int i = 0; i < pAI.length; i++) {
			int randomNumber = getRandomNumber();
			randomNumbersOnPanelsAI[i] = randomNumber;
			JLabel label = new JLabel(String.valueOf(randomNumber));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setVerticalAlignment(SwingConstants.BOTTOM);
			label.setSize(pAI[i].getWidth(), pAI[i].getHeight());
			pAI[i].setLayout(new BorderLayout());
			pAI[i].add(label, BorderLayout.WEST);
		}
	}

	void buttonListener(ActionEvent e, JButton[] b) {
		JButton cB = (JButton) e.getSource();
		int clI = -1;
		for (int i = 0; i < b.length; i++) {
			if (b[i] == cB) {
				clI = i;
				break;
			}
		}
		Color cC = b[clI].getBackground();
		playerActionPanel = new JPanel();
		playerActionPanel.setBackground(cC);
		setComponentSize(playerActionPanel, PANEL_WIDTH, PANEL_HEIGHT);
		panel1Top.remove(cB);
		revalidateAndRepaint(panel1Top);
		int cI = -1;
		for (int i = 0; i < b.length; i++) {
			if (b[i] == cB) {
				cI = i;
				break;
			}
		}
		if (cI != -1) {
			b[cI] = null;
		}
		List<Integer> iI = new ArrayList<>();
		for (int i = 0; i < b.length; i++) {
			if (b[i] != null && !b[i].isVisible())
				iI.add(i);
		}
		randIndexVisiblePanelAI = getRandomVisiblePanelIndex();
		Color pC = null;
		if (randIndexVisiblePanelAI != -1) {
			pC = panelArrayForAI[randIndexVisiblePanelAI].getBackground();
			panel3Top.remove(panelArrayForAI[randIndexVisiblePanelAI]);
			panelArrayForAI[randIndexVisiblePanelAI] = null;
			revalidateAndRepaint(panel3Top);
		}
		aiActionPanel = new JPanel();
		if (!iI.isEmpty()) {
			int rI = iI.get(getRandomNumber(iI.size()));
			b[rI].setVisible(true);
			panel1Top.add(b[rI]);
			revalidateAndRepaint(panel1Top);
		}
		aiActionPanel.setBackground(pC);
		setComponentSize(aiActionPanel, PANEL_WIDTH, PANEL_HEIGHT);
		int rIPI = getRandomInvisiblePanelIndex();
		if (rIPI != -1) {
			panelArrayForAI[rIPI].setVisible(true);
			panel3Top.add(panelArrayForAI[rIPI]);
			revalidateAndRepaint(panel3Top);
		}
		actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
		actionPanel.setBackground(grayColor);
		actionPanel.add(playerActionPanel);
		actionPanel.add(Box.createVerticalStrut(10));
		actionPanel.add(aiActionPanel);
		panel2Middle.add(actionPanel);
		revalidateAndRepaint(panel2Middle);
		disableOtherButtons(cB);
		stateManager = 1;
		showRandomNumberButton();
		toggleTimerStatus(true);
		clearLabels();
		clicksCounter = 3;
		clickedButtonIndex = Integer.parseInt(e.getActionCommand());
		int randomNumber = -1;
		if (randIndexVisiblePanelAI != -1) {
			randomNumber = randomNumbersOnPanelsAI[randIndexVisiblePanelAI];
		}
		JLabel playerNumberLabel = new JLabel(String.valueOf(randomNumbersOnButtonsPlayer[clickedButtonIndex]));
		playerNumberLabel.setHorizontalAlignment(SwingConstants.LEFT);
		playerNumberLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		playerNumberLabel.setSize(playerActionPanel.getWidth(), playerActionPanel.getHeight());
		playerActionPanel.setLayout(new BorderLayout());
		playerActionPanel.add(playerNumberLabel, BorderLayout.WEST);
		if (randomNumber != -1) {
			JLabel aiNumberLabel = new JLabel(String.valueOf(randomNumber));
			aiNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			aiNumberLabel.setVerticalAlignment(SwingConstants.BOTTOM);
			aiNumberLabel.setSize(aiActionPanel.getWidth(), aiActionPanel.getHeight());
			aiActionPanel.setLayout(new BorderLayout());
			aiActionPanel.add(aiNumberLabel, BorderLayout.EAST);
		}
		playerActionPanel.revalidate();
		playerActionPanel.repaint();
		aiActionPanel.revalidate();
		aiActionPanel.repaint();
	}

	void fourRoundsBattle() {
	    fightTimer = new Timer(1000, e -> {
			int result = playerTurn ? calculateDamage(randomNumberButton, randomNumbersOnButtonsPlayer, clickedButtonIndex)
												: calculateDamage(randomNumberButtonAI, randomNumbersOnPanelsAI, randIndexVisiblePanelAI);
			
	        updateDamage(playerTurn, result);
	        animateHit(playerTurn);
	        animateDamage(playerTurn);
	        playerTurn = !playerTurn;
			
	        if (++turnsCounter >= 8) {
	        	fightTimer.stop();
	        	declareWinnerOfTheRound();
	        }
	    });
	    fightTimer.start();
	}

	int calculateDamage(JButton button, int[] array, int index) {
	    return Integer.parseInt(button.getText()) * array[index];
	}
	
	void updateDamage(boolean turn, int result) {
	    if (turn) {
	        finalDamagePlayer = updateLabelAndFinalDamage(playerDamageLabel, finalDamagePlayer, result);
	    } else {
	        finalDamageAI = updateLabelAndFinalDamage(aiDamageLabel, finalDamageAI, result);
	    }
	}

	int updateLabelAndFinalDamage(JLabel damageLabel, int finalDamage, int result) {
	    finalDamage += result;
	    damageLabel.setText(String.valueOf(finalDamage));
	    return finalDamage;
	}

	void animateHit(boolean turn) {
	    JPanel targetPanel = turn ? aiActionPanel : playerActionPanel;
	    Color originalColor = targetPanel.getBackground();
	    targetPanel.setBackground(Color.WHITE);
	    Timer colorRestoreTimer = new Timer(300, e -> targetPanel.setBackground(originalColor));
	    colorRestoreTimer.start();
	}
	
	void animateDamage(boolean turn) {
	    timerDmg = new Timer(1, e -> {
	        if (turn) {
	            updateDamageAndLabel(finalPlayerDmg, finalDamagePlayer, playerDamageLabel, 10);
	        } else {
	            updateDamageAndLabel(finalAIDmg, finalDamageAI, aiDamageLabel, 10);
	        }
	    });
	    timerDmg.start();
	}

	void updateDamageAndLabel(AtomicInteger currentDamage, int finalDamage, JLabel damageLabel, int increment) {
	    int newValue = Math.min(currentDamage.get() + increment, finalDamage);
	    if (currentDamage.get() < finalDamage) {
	        currentDamage.getAndAdd(increment);
	        damageLabel.setText(String.valueOf(newValue));
	    } else {
	        timerDmg.stop();
	    }
	}

	void declareWinnerOfTheRound() {
		String winStatus;
		if (finalDamagePlayer > finalDamageAI) {
		    winStatus = "Player wins!";
		    playerWinsCounter++;
		    playerWinsLabel.setText("" + playerWinsCounter);
		} else if (finalDamageAI > finalDamagePlayer) {
		    winStatus = "AI wins!";
		    aiWinsCounter++;
		    aiWinsLabel.setText("" + aiWinsCounter);
		} else {
		    winStatus = "It's a draw!";
		}
		
		roundNumberLabel.setText(winStatus);
        finalDamagePlayer = 0;
        finalDamageAI = 0;
        turnsCounter = 0;
		enableAllButtons();
		panel2Middle.remove(actionPanel);
		revalidateAndRepaint(panel2Middle);
		timerDelay.start();

		timerRound = new Timer(1000, e -> {
			updateRoundNumber(++roundCounter);
			timerRound.stop();
		});
		timerRound.start();
	}

	void showRandomNumberButton() {
		randomNumberGeneratorButton.setVisible(true);
	}

	void activateRandomVisibleButton(JButton[] b) {
		List<Integer> vI = new ArrayList<>();
		for (int i = 0; i < b.length; i++) {
			if (b[i] != null && b[i].isVisible())
				vI.add(i);
		}
		if (!vI.isEmpty()) {
			int rI = vI.get(getRandomNumber(vI.size()));
			buttonListener(new ActionEvent(b[rI], ActionEvent.ACTION_PERFORMED, String.valueOf(rI)), b);
		}
	}

	int getRandomNumber() {
		return ThreadLocalRandom.current().nextInt(1, 100);
	}

	int getRandomNumber(int n) {
		return ThreadLocalRandom.current().nextInt(n);
	}

	void hideButtons() {
		randomNumberGeneratorButton.setVisible(false);
		randomNumberButton.setVisible(false);
	}

	void toggleTimerStatus(boolean s) {
		progressBar.setValue(5);
		progressBar.setString(Integer.toString(5));
		if (s) {
			timerDelay.start();
		} else {
			timerDelay.stop();
		}
	}

	void formatButton(JButton s) {
		s.setFocusable(false);
		s.setBorderPainted(false);
		s.setBackground(lightGrayColor);
		s.setFont(new Font(s.getFont().getName(), Font.PLAIN, 30));
	}

	void updateLabels() {
		String randNumPlayer = randomNumberButton.getText();
		String randNumAI = randomNumberButtonAI.getText();
		playerDamageLabel.setText(randNumPlayer);
		aiDamageLabel.setText(randNumAI);
	}

	void clearLabels() {
		playerDamageLabel.setText(" ");
		aiDamageLabel.setText(" ");
	}

	int getRandomVisiblePanelIndex() {
		List<Integer> vPI = new ArrayList<>();
		for (int i = 0; i < panelArrayForAI.length; i++) {
			if (panelArrayForAI[i] != null && panelArrayForAI[i].isVisible())
				vPI.add(i);
		}
		if (!vPI.isEmpty()) {
			return vPI.get(getRandomNumber(vPI.size()));
		} else {
			return -1;
		}
	}

	int getRandomInvisiblePanelIndex() {
		List<Integer> iPI = new ArrayList<>();
		for (int i = 0; i < panelArrayForAI.length; i++) {
			if (panelArrayForAI[i] != null && !panelArrayForAI[i].isVisible())
				iPI.add(i);
		}
		if (!iPI.isEmpty()) {
			return iPI.get(getRandomNumber(iPI.size()));
		} else {
			return -1;
		}
	}

	void shuffleArrays(List<Color> a, List<Integer> b) {
		Collections.shuffle(a);
		Collections.shuffle(b);
	}

	void disableOtherButtons(JButton clickedButton) {
		for (JButton button : buttonArrayForPlayer) {
			if (button != clickedButton && button != null) {
				button.setEnabled(false);
			}
		}
	}

	void enableAllButtons() {
		if (buttonArrayForPlayer != null) {
			for (JButton button : buttonArrayForPlayer) {
				if (button != null) {
					button.setEnabled(true);
				}
			}
		}
	}

	void setComponentSize(JComponent component, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
	}

	void createBorderBox(JPanel p, Color c, int s) {
		Border bf = BorderFactory.createLineBorder(c, s);
		p.setBorder(bf);
	}

	void revalidateAndRepaint(JPanel p) {
		p.revalidate();
		p.repaint();
	}

	void hideAndClearButtons() {
		hideButtons();
		updateLabels();
		stateManager = 0;
		toggleTimerStatus(false);
	}

	void setButtonVisibility(JComponent[] compo, List<Color> colo, List<Integer> visInd) {
		for (int i = 0; i < 5; i++) {
			compo[i].setBackground(colo.get(i));
			compo[i].setVisible(visInd.indexOf(i) < 3);
		}
	}

	void updateRoundNumber(int round) {
		if (round <= 5)
			roundNumberLabel.setText("" + round);
		else {
			endOfTheBattle();
		}
	}
	
	void clearWinsLabels() {
		playerWinsLabel.setText("");
		aiWinsLabel.setText("");
	}
	
	void endOfTheBattle() {
		String winStatus;
		if (playerWinsCounter > aiWinsCounter) {
			winStatus = "Victory!";
			clearWinsLabels();
			clearLabels();
		} else if (playerWinsCounter < aiWinsCounter) {
			winStatus = "Defeat!";
			clearWinsLabels();
			clearLabels();
		} else {
			winStatus = "Draw!";
		}
		roundNumberLabel.setText(winStatus);
		toggleTimerStatus(false);
		
		roundCounter = 0;
		playerWinsCounter = 0;
		aiWinsCounter = 0;
	}
	
	public static void main(String[] args) {
		new BattleSystem();
	}
}

package orca.scene.tester;

import java.util.ArrayList;

import orca.scene.tester.interfaces.Call;

public class OrcaTester extends OrcaApi {

	// Main method
	public static void main(String[] args) {
		int timeout = 0;
		int loops = 1;

		if (args.length < 2) { 
			System.out.println("Params: <scene> <loops> [<timeout>]");
			return;
		}
		
		// Prepare test tool
		OrcaTester tester = new OrcaTester();
		
		// Read params
		loops = Integer.parseInt(args[1]);
		if (args.length > 2)
			timeout = Integer.parseInt(args[2]) * 1000;
		
		int scene = Integer.parseInt(args[0]);
		Scene s = tester.getScene(scene);
		if (s == null) {
			System.out.println(scene + " is not a valid scene number");
			return;
		}
		
		System.out.println("Starting scene " + scene);
		System.out.println("Executing " + loops + " loop(s)");
		System.out.println("Using timeout " + timeout + " ms");
		
		// Launch the test
		tester.run("http://127.0.0.1:8080/gateway", 8081, loops, timeout, s);
		//tester.run("http://rabbit.rabbitmq.wall2-ilabt-iminds-be.wall1.ilabt.iminds.be:8080/gateway", 8081, loops, timeout, s);
		//tester.run("http://10.10.1.28:8080/gateway", 8081, loops, timeout, s);
	}
	
	// Variables
	private ArrayList<Scene> scenes;
	
	public Scene getScene(int index) {
		if (scenes != null) {
			return scenes.get(index);
		}
		
		return null;
	}
	
	/**
	 * Constructor
	 */
	@SuppressWarnings("serial")
	public OrcaTester() {
		// Init scenes array
		scenes = new ArrayList<Scene>() {
			{
				add(0, getEndDemoScene1());
				add(1, getEndDemoScene2());
				add(2, getEndDemoScene3());
				add(3, testThis());
			}
		};
	}
	
	/**
	 * Create a test scene
	 */
	private Scene getTestScene() {
//		return new Scene(new Call[] {
//				new Call() { public String e() { return location_update_karel_corridor(); }},
//				new Call() { public String e() { return location_update_lisa_corridor(); }}
//		}, null, new Call[] {
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }},
//				new Call() { public String e() { return call_launch_erik(); }}
//		});
		
		Scene scene = new Scene(null, null, 
				// Scene calls
				new Call[] {
					new Call() { public String e() { return location_update_karel_patient(); }},
					//new Call() { public String e() { return location_update_lisa_corridor(); }},
					//new Call() { public String e() { return location_update_marie_corridor(); }}
				}
			);
			
			return scene;
	}
	
	private Scene getEndDemoScene1() {
		Scene scene = new Scene(new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
			}, null, new Call[] {
				new Call() { public String e() { return login_karel(); }},
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept(); }},
				new Call() { public String e() { return presence_off_karel_room2(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				new Call() { public String e() { return call_karel_update_reason(); }},
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return logoff_karel(); }}
			});
		
		return scene;
	}
	
	private Scene getEndDemoScene2() {
		Scene scene = new Scene(new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
			}, null, new Call[] {
				new Call() { public String e() { return login_karel(); }},
				new Call() { public String e() { return login_lisa(); }},
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_lisa_temp_accept(); }}, 
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
				new Call() { public String e() { return call_lisa_update_reason(); }},
				new Call() { public String e() { return presence_off_lisa(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return logoff_karel(); }},
				new Call() { public String e() { return logoff_lisa(); }}
			});
		
		return scene;
	}
	
	private Scene getEndDemoScene3() {
		Scene scene = new Scene(new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
			}, null, new Call[] {
				new Call() { public String e() { return login_karel(); }},
				new Call() { public String e() { return login_lisa(); }},
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept(); }}, 
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				new Call() { public String e() { return call_karel_assistance(); }},
				new Call() { public String e() { return call_karel_assistance_lisa_temp_accept(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
				new Call() { public String e() { return call_karel_assistance_lisa_update_reason(); }},
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return presence_off_lisa(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return logoff_karel(); }},
				new Call() { public String e() { return logoff_lisa(); }}
			});
		
		return scene;
	}
	
	private Scene testThis() {
		return new Scene(
			new Call[] {
					new Call() { public String e() { return location_update_karel_corridor(); }}
				}, null, new Call[] {
					new Call() { public String e() { return call_launch_erik(); }},
					new Call() { public String e() { return call_launch_erik(); }}
			});
	}
	
	/**
	 * Scene 1
	 */
	private Scene getScene1() {
		Scene scene = new Scene(null, null, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 2
	 */
	private Scene getScene2() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }}
			},
			// Reset calls
			null
			, 
			// Scene calls	
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return presence_on_karel_room2(); }},
				new Call() { public String e() { return presence_off_karel_room2(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 3
	 */
	private Scene getScene3() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			},
			// Reset calls
			null
			, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 4
	 */
	private Scene getScene4() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			},
			// Reset calls
			null
			,
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_sarah(); }},
				new Call() { public String e() { return call_karel_temp_accept_hotel_room2(); }},
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return presence_on_karel_room2(); }},
				new Call() { public String e() { return presence_off_karel_room2(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 5
	 */
	private Scene getScene5() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return presence_on_karel_room2(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			},
			// Reset calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return presence_on_karel_room2(); }},
			},
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept_medical(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 6
	 */
	private Scene getScene6() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return location_update_marie_patient(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			}, 
			// Reset calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient(); }}
			}, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_sarah(); }},
				new Call() { public String e() { return call_lisa_redirect(); }},
				new Call() { public String e() { return call_karel_temp_accept_room2(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return presence_on_karel_room2(); }},
				new Call() { public String e() { return presence_off_karel_room2(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Scene 7
	 */
	private Scene getScene7() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return location_update_marie_patient(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			}, 
			// Reset calls
			new Call[] {
				new Call() { public String e() { return location_update_marie_patient(); }}
			}, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_sarah(); }},
				new Call() { public String e() { return call_lisa_redirect_medical(); }},
				new Call() { public String e() { return call_marie_temp_accept_medical(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }},
				new Call() { public String e() { return location_update_marie_patient_room2(); }},
				new Call() { public String e() { return presence_on_marie_room2(); }},
				new Call() { public String e() { return presence_off_marie_room2(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Multiple calls - 1 active call
	 */
	private Scene getsceneMultipleCalls1() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }}
		});
	
		return scene;
	}
	
	/**
	 * Multiple calls - 2 active calls
	 */
	private Scene getsceneMultipleCalls2() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }},
				new Call() { public String e() { return call_launch_nina(); }}
		});
	
		return scene;
	}
	
	/**
	 * Multiple calls - 3 active calls
	 */
	private Scene getsceneMultipleCalls3() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }},
				new Call() { public String e() { return call_launch_nina(); }},
				new Call() { public String e() { return call_launch_frederik(); }}
		});
	
		return scene;
	}
	
	/**
	 * Multiple calls - 5 active calls
	 */
	private Scene getsceneMultipleCalls5() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }},
				new Call() { public String e() { return call_launch_nina(); }},
				new Call() { public String e() { return call_launch_frederik(); }},
				new Call() { public String e() { return call_launch_bert(); }},
				new Call() { public String e() { return call_launch_robert(); }}
		});
	
		return scene;
	}
	
	/**
	 * Multiple calls - 10 active calls
	 */
	private Scene getsceneMultipleCalls10() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }},
				new Call() { public String e() { return call_launch_nina(); }},
				new Call() { public String e() { return call_launch_frederik(); }},
				new Call() { public String e() { return call_launch_bert(); }},
				new Call() { public String e() { return call_launch_robert(); }},
				new Call() { public String e() { return call_launch_gilbert(); }},
				new Call() { public String e() { return call_launch_arthur(); }},
				new Call() { public String e() { return call_launch_lucas(); }},
				new Call() { public String e() { return call_launch_luna(); }},
				new Call() { public String e() { return call_launch_lindsey(); }}
		});
	
		return scene;
	}
	
	/**
	 * Multiple calls - 10 active calls
	 */
	private Scene getsceneMultipleCalls16() {
		Scene scene = getScene7();
		
		// Add calls
		scene.addSetupCallsToFront(new Call[] {
				new Call() { public String e() { return call_launch_george(); }},
				new Call() { public String e() { return call_launch_nina(); }},
				new Call() { public String e() { return call_launch_frederik(); }},
				new Call() { public String e() { return call_launch_bert(); }},
				new Call() { public String e() { return call_launch_robert(); }},
				new Call() { public String e() { return call_launch_gilbert(); }},
				new Call() { public String e() { return call_launch_arthur(); }},
				new Call() { public String e() { return call_launch_lucas(); }},
				new Call() { public String e() { return call_launch_luna(); }},
				new Call() { public String e() { return call_launch_lindsey(); }},
				new Call() { public String e() { return call_launch_ken(); }},
				new Call() { public String e() { return call_launch_sander(); }},
				new Call() { public String e() { return call_launch_jana(); }},
				new Call() { public String e() { return call_launch_karen(); }},
				new Call() { public String e() { return call_launch_jun(); }},
				new Call() { public String e() { return call_launch_erik(); }},
		});
	
		return scene;
	}
	
	/**
	 * Changes meeting - Use case 1 normal call
	 * @return
	 */
	private Scene getSceneChangeUseCase1NormalCall() {
		Scene scene = new Scene(
			// Setup calls
			null, 
			// Reset calls
			null, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik_pain(); }},
			}
		);
		
		return scene;
	}
	
	/**
	 * Assistance call
	 * @return
	 */
	private Scene getSceneAssistanceCall() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return location_update_marie_patient_room2(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			}, 
			// Reset calls
			null, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept_assistance_scene(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				new Call() { public String e() { return call_karel_assistance(); }},
				new Call() { public String e() { return call_lisa_temp_accept_assistance_call(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return presence_off_lisa(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }}
			}
		);
		
		return scene;
	}
	
	/**
	 * Assistance call 2
	 * @return
	 */
	private Scene getSceneAssistanceCall2() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return location_update_lisa_patient_room2(); }},
				new Call() { public String e() { return location_update_marie_patient_room2(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			}, 
			// Reset calls
			null, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept_assistance_scene(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				
				new Call() { public String e() { return call_karel_assistance(); }},
				new Call() { public String e() { return call_lisa_temp_accept_assistance_call(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
				
				new Call() { public String e() { return call_lisa_assistance2(); }},
				new Call() { public String e() { return call_marie_temp_accept_assistance_call2(); }},
				new Call() { public String e() { return location_update_marie_patient(); }},
				new Call() { public String e() { return presence_on_marie(); }},
				
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return presence_off_lisa(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return presence_off_marie(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
				
			}
		);
		
		return scene;
	}
	
	/**
	 * Assistance call 3
	 * @return
	 */
	private Scene getSceneAssistanceCallRedirect() {
		Scene scene = new Scene(
			// Setup calls
			new Call[] {
				new Call() { public String e() { return location_update_karel_patient_room2(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return location_update_marie_patient_room2(); }},
				new Call() { public String e() { return call_set_personalrelationship_degree(8); }}
			}, 
			// Reset calls
			null, 
			// Scene calls
			new Call[] {
				new Call() { public String e() { return call_launch_erik(); }},
				new Call() { public String e() { return call_karel_temp_accept_assistance_scene(); }},
				new Call() { public String e() { return location_update_karel_patient(); }},
				new Call() { public String e() { return presence_on_karel(); }},
				
				new Call() { public String e() { return call_karel_assistance(); }},
				new Call() { public String e() { return call_lisa_redirect_medical_assistance_call(); }},
				new Call() { public String e() { return call_marie_temp_accept_assistance_call(); }},
				new Call() { public String e() { return location_update_marie_patient(); }},
				new Call() { public String e() { return presence_on_marie(); }},
				
				new Call() { public String e() { return call_marie_assistance2(); }},		
				new Call() { public String e() { return call_lisa_temp_accept_assistance_call2(); }},
				new Call() { public String e() { return location_update_lisa_patient(); }},
				new Call() { public String e() { return presence_on_lisa(); }},
								
				new Call() { public String e() { return presence_off_karel(); }},
				new Call() { public String e() { return location_update_karel_corridor(); }},
				new Call() { public String e() { return presence_off_lisa(); }},
				new Call() { public String e() { return location_update_lisa_corridor(); }},
				new Call() { public String e() { return presence_off_marie(); }},
				new Call() { public String e() { return location_update_marie_corridor(); }}
			}
		);
		
		return scene;
	}
	
	
	
}

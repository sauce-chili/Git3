using System.Collections;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using UnityEngine;

public class FPSInput : MonoBehaviour
{
    [SerializeField] private float speedPlayer;
    private CharacterController player;
    public float gravity = 20f;
    public const float baseSpeed = 6.0f;
    public const float baseSpeedShip = 6.0f;
    // Start is called before the first frame update
    void Start()
    {
        player = GetComponent<CharacterController>();
    }

    void Awake()
    {
        Messenger<float>.AddListener(GameEvent.SPEED_CHANGED, OnSpeedChanged);
    }
    void OnDestroy()
    {
        Messenger<float>.RemoveListener(GameEvent.SPEED_CHANGED, OnSpeedChanged);
    }
    private void OnSpeedChanged(float value)
    {
        speedPlayer = baseSpeed * value;
    }

    // Update is called once per frame
    void Update()
    {
        float deltaX = Input.GetAxis("Vertical") * speedPlayer; ;
        float deltaZ = -Input.GetAxis("Horizontal") * speedPlayer;
        Vector3 movement = new Vector3(deltaX, 0, deltaZ);

        movement = Vector3.ClampMagnitude(movement, speedPlayer);
        movement.y = gravity;

        movement *= Time.deltaTime;
        movement = transform.TransformDirection(movement);
        player.Move(movement);
    }
}

import { useState, useRef, useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { 
  Card, 
  CardContent, 
  CardDescription,
  CardFooter, 
  CardHeader, 
  CardTitle 
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { otpVerificationSchema } from "@/lib/validation/auth"
import type { OtpVerificationFormValues } from "@/lib/validation/auth"
import { Loader2 } from "lucide-react"

const OTPVerificationPage = () => {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [countDown, setCountDown] = useState(30)
  const [canResend, setCanResend] = useState(false)
  const [inputValues, setInputValues] = useState(["", "", "", "", "", ""])
  const inputRefs = Array(6).fill(0).map(() => useRef<HTMLInputElement>(null))
  
  // Countdown timer for OTP resend
  useEffect(() => {
    if (countDown <= 0) {
      setCanResend(true)
      return
    }
    
    const timer = setTimeout(() => setCountDown(countDown - 1), 1000)
    return () => clearTimeout(timer)
  }, [countDown])
  
  // Handle OTP input changes
  const handleOtpChange = (index: number, value: string) => {
    // Allow only numbers
    if (!/^\d*$/.test(value)) return
    
    const newInputValues = [...inputValues]
    newInputValues[index] = value
    setInputValues(newInputValues)
    
    // Auto focus to next input if current input is filled
    if (value && index < 5) {
      inputRefs[index + 1].current?.focus()
    }
    
    // Update form value
    const otpValue = newInputValues.join("")
    form.setValue("otp", otpValue)
  }
  
  // Handle key down events for backspace and arrow keys
  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !inputValues[index] && index > 0) {
      inputRefs[index - 1].current?.focus()
    } else if (e.key === "ArrowLeft" && index > 0) {
      inputRefs[index - 1].current?.focus()
    } else if (e.key === "ArrowRight" && index < 5) {
      inputRefs[index + 1].current?.focus()
    }
  }
  
  const form = useForm<OtpVerificationFormValues>({
    resolver: zodResolver(otpVerificationSchema),
    defaultValues: {
      otp: "",
    },
  })

  const onSubmit = async (data: OtpVerificationFormValues) => {
    try {
      setIsLoading(true)
      console.log("OTP verification data:", data)
      // TODO: Implement actual OTP verification logic with API
      
      // Simulate API call delay
      await new Promise((resolve) => setTimeout(resolve, 1000))
      
      // Redirect to login page after successful verification
      navigate("/auth/login", { state: { verified: true } })
    } catch (error) {
      console.error("OTP verification failed:", error)
    } finally {
      setIsLoading(false)
    }
  }
  
  const handleResendOTP = async () => {
    if (!canResend) return
    
    try {
      // TODO: Implement actual OTP resend logic with API
      console.log("Resending OTP...")
      
      // Reset timer
      setCountDown(30)
      setCanResend(false)
      
      // Clear input fields
      setInputValues(["", "", "", "", "", ""])
      form.setValue("otp", "")
      inputRefs[0].current?.focus()
    } catch (error) {
      console.error("Failed to resend OTP:", error)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Verification Code</CardTitle>
        <CardDescription>
          We have sent a 6-digit verification code to your email address
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <FormField
              control={form.control}
              name="otp"
              render={() => (
                <FormItem>
                  <div className="flex justify-center gap-2">
                    {inputValues.map((value, index) => (
                      <FormControl key={index}>
                        <Input
                          ref={inputRefs[index]}
                          value={value}
                          onChange={(e) => handleOtpChange(index, e.target.value)}
                          onKeyDown={(e) => handleKeyDown(index, e)}
                          className="h-12 w-12 text-center text-xl"
                          maxLength={1}
                        />
                      </FormControl>
                    ))}
                  </div>
                  <div className="text-center mt-1">
                    <FormMessage />
                  </div>
                </FormItem>
              )}
            />
            
            <div className="text-center">
              <p className="text-sm text-muted-foreground">
                Didn't receive the code?{" "}
                <button
                  type="button"
                  onClick={handleResendOTP}
                  className={`font-medium ${
                    canResend
                      ? "text-primary hover:underline cursor-pointer"
                      : "text-muted-foreground cursor-not-allowed"
                  }`}
                  disabled={!canResend}
                >
                  {canResend
                    ? "Resend code"
                    : `Resend code (${countDown}s)`}
                </button>
              </p>
            </div>
            
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Verifying...
                </>
              ) : (
                "Verify"
              )}
            </Button>
          </form>
        </Form>
      </CardContent>
      <CardFooter className="flex justify-center">
        <p className="text-center text-sm text-muted-foreground">
          <Link
            to="/auth/login"
            className="font-medium text-primary hover:underline"
          >
            Back to login
          </Link>
        </p>
      </CardFooter>
    </Card>
  )
}

export default OTPVerificationPage

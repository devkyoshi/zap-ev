import { useState } from "react"
import { Link } from "react-router-dom"
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
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { forgotPasswordSchema } from "@/lib/validation/auth"
import type { ForgotPasswordFormValues } from "@/lib/validation/auth"
import { Loader2 } from "lucide-react"

const ForgotPasswordPage = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitted, setIsSubmitted] = useState(false)
  
  const form = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: "",
    },
  })

  const onSubmit = async (data: ForgotPasswordFormValues) => {
    try {
      setIsLoading(true)
      console.log("Forgot password request:", data)
      // TODO: Implement actual forgot password logic with API
      
      // Simulate API call delay
      await new Promise((resolve) => setTimeout(resolve, 1000))
      
      setIsSubmitted(true)
    } catch (error) {
      console.error("Password reset request failed:", error)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reset Password</CardTitle>
        <CardDescription>
          Enter your email address to receive a password reset link
        </CardDescription>
      </CardHeader>
      <CardContent>
        {isSubmitted ? (
          <div className="text-center space-y-4">
            <div className="rounded-full bg-primary/10 p-3 text-primary inline-block">
              <svg 
                xmlns="http://www.w3.org/2000/svg" 
                width="24" 
                height="24" 
                viewBox="0 0 24 24" 
                fill="none" 
                stroke="currentColor" 
                strokeWidth="2" 
                strokeLinecap="round" 
                strokeLinejoin="round"
              >
                <path d="M22 10.5V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h16a2 2 0 0 0 2-2v-4.5" />
                <path d="M22 6l-10 7L2 6" />
              </svg>
            </div>
            <h3 className="text-lg font-medium">Check your email</h3>
            <p className="text-sm text-muted-foreground">
              We've sent a password reset link to your email address. Please check your inbox.
            </p>
            <Button asChild className="w-full mt-4">
              <Link to="/auth/login">Back to Login</Link>
            </Button>
          </div>
        ) : (
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input placeholder="name@example.com" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  "Send Reset Link"
                )}
              </Button>
            </form>
          </Form>
        )}
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

export default ForgotPasswordPage
